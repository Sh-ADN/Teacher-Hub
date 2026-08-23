package com.abutorab.teacher.hub.domain

import com.abutorab.teacher.hub.data.MarkEntity
import com.abutorab.teacher.hub.data.StudentEntity
import com.abutorab.teacher.hub.data.SubjectEntity

data class SubjectResult(
    val subject: SubjectEntity,
    val mcq: Int?,
    val written: Int?,
    val practical: Int?
) {
    val subjectId: String get() = subject.id
    val total: Int get() = (mcq ?: 0) + (written ?: 0) + (practical ?: 0)
    val grade: Grade get() = calculateGrade(total, subject.maxMarks)
}

data class StudentWithMark(
    val student: StudentEntity,
    val mark: MarkEntity?
)

typealias StudentMarkRow = StudentWithMark

data class SubjectMark(
    val subject: SubjectEntity,
    val mark: MarkEntity?
)

data class MarksheetData(
    val student: StudentEntity,
    val subjectMarks: List<SubjectMark>,
    val totalMcq: Int,
    val totalWritten: Int,
    val totalPractical: Int,
    val grandTotal: Int,
    val gpa: Double,
    val grade: String
)

data class TabulationRow(
    val student: StudentEntity,
    val results: Map<String, SubjectResult>, // Key is SubjectEntity.id
    val totalMarks: Int,
    val finalGpa: Double,
    val finalGrade: String,
    val failedSubjectCount: Int = 0,
    val meritPosition: Int = 0
) {
    val gpa: Double get() = finalGpa
    val grade: String get() = finalGrade
    val subjectMarksMap: Map<String, MarkEntity?>
        get() = results.mapValues { (_, res) ->
            if (res.mcq != null || res.written != null || res.practical != null) {
                MarkEntity(
                    rollNumber = student.rollNumber,
                    subjectId = res.subject.id,
                    mcq = res.mcq,
                    written = res.written,
                    practical = res.practical
                )
            } else {
                null
            }
        }
}
