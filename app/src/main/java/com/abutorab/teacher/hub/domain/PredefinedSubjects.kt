package com.abutorab.teacher.hub.domain

data class PredefinedSubject(val id: String, val title: String)

val PREDEFINED_SUBJECTS = listOf(
    PredefinedSubject("101", "Bangla 1st Paper"),
    PredefinedSubject("102", "Bangla 2nd Paper"),
    PredefinedSubject("107", "English 1st Paper"),
    PredefinedSubject("108", "English 2nd Paper"),
    PredefinedSubject("109", "Mathematics"),
    PredefinedSubject("111", "Religion"),
    PredefinedSubject("150", "Bangladesh and Global Studies"),
    PredefinedSubject("127", "Science"),
    PredefinedSubject("154", "Information and Communication Technology"),
    PredefinedSubject("147", "Physical Education, Health and Sports"),
    PredefinedSubject("148", "Arts and Crafts"),
    PredefinedSubject("156", "Career Education"),
    PredefinedSubject("134", "Agriculture Studies"),
    PredefinedSubject("151", "Home Science")
)
