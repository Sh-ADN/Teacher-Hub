package com.abutorab.teacher.hub.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY title ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    // Students
    @Query("SELECT * FROM students ORDER BY rollNumber ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("SELECT * FROM students WHERE rollNumber = :roll")
    suspend fun getStudentByRoll(roll: Int): StudentEntity?

    // Marks
    @Query("SELECT * FROM marks WHERE subjectId = :subjectId")
    fun getMarksForSubject(subjectId: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE rollNumber = :roll")
    fun getMarksForStudent(roll: Int): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks")
    fun getAllMarks(): Flow<List<MarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: MarkEntity)
}
