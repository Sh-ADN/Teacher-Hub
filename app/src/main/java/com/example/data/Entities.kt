package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val maxMarks: Int,
    val passMarks: Int,
    val hasMcq: Boolean = true,
    val maxMcq: Int = 100,
    val hasWritten: Boolean = true,
    val maxWritten: Int = 100,
    val hasPractical: Boolean = true,
    val maxPractical: Int = 0
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val rollNumber: Int,
    val name: String
)

@Entity(
    tableName = "marks",
    primaryKeys = ["rollNumber", "subjectId"],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["rollNumber"],
            childColumns = ["rollNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MarkEntity(
    val rollNumber: Int,
    val subjectId: String,
    val mcq: Int? = null,
    val written: Int? = null,
    val practical: Int? = null
)
