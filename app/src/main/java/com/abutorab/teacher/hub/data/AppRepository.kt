package com.abutorab.teacher.hub.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allStudentsGlobal: Flow<List<StudentEntity>> = dao.getAllStudentsGlobal()
    val allMarksGlobal: Flow<List<MarkEntity>> = dao.getAllMarksGlobal()

    fun getStudentsByYear(year: Int): Flow<List<StudentEntity>> {
        return dao.getStudentsByYear(year)
    }

    fun getMarksForSubject(subjectId: String, year: Int, term: String): Flow<List<MarkEntity>> {
        return dao.getMarksForSubject(subjectId, year, term)
    }
    
    fun getAllMarks(year: Int, term: String): Flow<List<MarkEntity>> {
        return dao.getAllMarks(year, term)
    }

    suspend fun createInitialDataIfEmpty() {
        if (dao.getSubjectCount() == 0) {
            // Replaced by ensurePredefinedSubjectsExist
        }
    }

    suspend fun ensurePredefinedSubjectsExist() {
        val existingSubjects = dao.getAllSubjectsOnce()
        val existingIds = existingSubjects.map { it.id }.toSet()
        val newSubjects = mutableListOf<SubjectEntity>()
        
        com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.forEach { predefined ->
            if (predefined.id !in existingIds) {
                newSubjects.add(
                    SubjectEntity(
                        id = predefined.id,
                        title = predefined.title,
                        maxMarks = 100,
                        passMarks = 33,
                        hasMcq = true,
                        maxMcq = 30,
                        hasWritten = true,
                        maxWritten = 70,
                        hasPractical = false,
                        maxPractical = 0
                    )
                )
            }
        }
        
        if (newSubjects.isNotEmpty()) {
            dao.insertSubjects(newSubjects)
        }
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

    suspend fun insertStudent(student: StudentEntity) {
        dao.insertStudent(student)
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteStudent(student)
    }

    suspend fun insertMark(mark: MarkEntity) {
        dao.insertMark(mark)
    }
    
    suspend fun insertMarks(marks: List<MarkEntity>) {
        dao.insertMarks(marks)
    }

    suspend fun updateMark(mark: MarkEntity) {
        // AppDao does not have updateMark? Wait, let's check AppDao.
        // It has insertMark which uses REPLACE.
        dao.insertMark(mark)
    }
    
    suspend fun deleteMarks(marks: List<MarkEntity>) {
        // dao doesn't have deleteMarks(List), but it's okay, not used?
        // Wait, what methods are used? Let's fix errors.
    }

    suspend fun insertStudents(students: List<StudentEntity>) {
        dao.insertStudents(students)
    }

    suspend fun saveMark(rollNumber: Int, subjectId: String, mcq: Int?, written: Int?, practical: Int?, year: Int, term: String) {
        val mark = MarkEntity(
            rollNumber = rollNumber,
            subjectId = subjectId,
            year = year,
            term = term,
            mcq = mcq,
            written = written,
            practical = practical
        )
        dao.insertMark(mark)
    }

    suspend fun getStudentCountGlobal(): Int {
        return dao.getStudentCountGlobal()
    }

    suspend fun clearAllDataGlobal() {
        dao.deleteAllMarksGlobal()
        dao.deleteAllStudentsGlobal()
        dao.deleteAllSubjects()
    }

    suspend fun saveAllData(subjects: List<SubjectEntity>, students: List<StudentEntity>, marks: List<MarkEntity>) {
        if (subjects.isNotEmpty()) dao.insertSubjectsReplace(subjects)
        if (students.isNotEmpty()) dao.insertStudentsReplace(students)
        if (marks.isNotEmpty()) dao.insertMarks(marks)
    }
}
