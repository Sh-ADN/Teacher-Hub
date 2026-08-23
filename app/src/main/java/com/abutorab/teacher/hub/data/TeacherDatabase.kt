package com.abutorab.teacher.hub.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM students ORDER BY rollNumber ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY rollNumber ASC")
    fun getAllStudentsGlobal(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE year = :year ORDER BY rollNumber ASC")
    fun getStudentsByYear(year: Int): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentsReplace(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM subjects ORDER BY title ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY title ASC")
    suspend fun getAllSubjectsOnce(): List<SubjectEntity>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectsReplace(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("UPDATE marks SET subjectId = :newSubjectId WHERE subjectId = :oldSubjectId")
    suspend fun transferSubjectMarks(oldSubjectId: String, newSubjectId: String)

    @Query("DELETE FROM marks WHERE subjectId = :subjectId")
    suspend fun deleteMarksForSubject(subjectId: String)

    @Query("DELETE FROM marks WHERE subjectId = :subjectId")
    suspend fun deleteAllMarksForSubject(subjectId: String)

    @Query("SELECT * FROM marks WHERE subjectId = :subjectId")
    suspend fun getAllMarksForSubject(subjectId: String): List<MarkEntity>

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("SELECT * FROM marks WHERE year = :year AND term = :term")
    fun getMarksForTerm(year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE year = :year AND term = :term")
    fun getAllMarks(year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE subjectId = :subjectId AND year = :year AND term = :term")
    fun getMarksForSubject(subjectId: String, year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks")
    fun getAllMarksGlobal(): Flow<List<MarkEntity>>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCountGlobal(): Int

    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("DELETE FROM students")
    suspend fun deleteAllStudentsGlobal()

    @Query("DELETE FROM marks")
    suspend fun clearMarks()

    @Query("DELETE FROM marks")
    suspend fun deleteAllMarksGlobal()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: MarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkEntity>)
}

typealias AppDao = TeacherDao

@Database(
    entities = [StudentEntity::class, SubjectEntity::class, MarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TeacherDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao

    companion object {
        @Volatile
        private var INSTANCE: TeacherDatabase? = null

        fun getDatabase(context: Context): TeacherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TeacherDatabase::class.java,
                    "teacher_hub_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
