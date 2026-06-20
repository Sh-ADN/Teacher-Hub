package com.abutorab.teacher.hub.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allMarks: Flow<List<MarkEntity>> = dao.getAllMarks()

    suspend fun createInitialDataIfEmpty() {
        if (dao.getSubjectCount() == 0) {
            val sampleSubjects = listOf(
                SubjectEntity("BAN1", "Bangla 1", 100, 33, hasMcq = true, maxMcq = 30, hasWritten = true, maxWritten = 70, hasPractical = false, maxPractical = 0),
                SubjectEntity("ENG1", "English 1", 100, 33, hasMcq = false, maxMcq = 0, hasWritten = true, maxWritten = 100, hasPractical = false, maxPractical = 0),
                SubjectEntity("MATH", "Math", 100, 33, hasMcq = true, maxMcq = 30, hasWritten = true, maxWritten = 70, hasPractical = false, maxPractical = 0),
                SubjectEntity("ICT", "ICT", 50, 17, hasMcq = true, maxMcq = 25, hasWritten = false, maxWritten = 0, hasPractical = true, maxPractical = 25)
            )
            dao.insertSubjects(sampleSubjects)
        }

        val sampleStudents = listOf(
            StudentEntity(1, "Aarav Roy"),
            StudentEntity(2, "Sadia Islam"),
            StudentEntity(3, "Rahim Ahmed"),
            StudentEntity(4, "Priya Das"),
            StudentEntity(5, "Nusrat Jahan")
        )
        dao.insertStudents(sampleStudents)
    }

    suspend fun insertSubject(subject: SubjectEntity) {
        dao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: SubjectEntity) {
        dao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.deleteSubject(subject)
    }

    fun getMarksForSubject(subjectId: String): Flow<List<MarkEntity>> {
        return dao.getMarksForSubject(subjectId)
    }

    suspend fun insertStudent(student: StudentEntity) {
        dao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<StudentEntity>) {
        dao.insertStudents(students)
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteStudent(student)
    }

    suspend fun saveMark(rollNumber: Int, subjectId: String, mcq: Int?, written: Int?, practical: Int?) {
        val mark = MarkEntity(
            rollNumber = rollNumber,
            subjectId = subjectId,
            mcq = mcq,
            written = written,
            practical = practical
        )
        dao.insertMark(mark)
    }
}
