package com.abutorab.teacher.hub.domain

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abutorab.teacher.hub.auth.AuthManager
import com.abutorab.teacher.hub.data.AppRepository
import com.abutorab.teacher.hub.data.CalculationUtils
import com.abutorab.teacher.hub.data.MarkEntity
import com.abutorab.teacher.hub.data.StudentEntity
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.data.TeacherDatabase
import com.abutorab.teacher.hub.sync.SyncManager
import com.abutorab.teacher.hub.util.ThemePreference
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.round

class TeacherViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        AppRepository(TeacherDatabase.getDatabase(application).teacherDao())
    )

    private val sharedPrefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val userPrefs = application.getSharedPreferences("teacher_hub_prefs", Context.MODE_PRIVATE)
    val authManager = AuthManager()

    private val _ardhoPercent = MutableStateFlow(sharedPrefs.getInt("somonnito_ardho_percent", 50))
    val ardhoPercent = _ardhoPercent.asStateFlow()

    fun notifySettingsChanged() {
        _ardhoPercent.value = sharedPrefs.getInt("somonnito_ardho_percent", 50)
    }

    val syncManager = SyncManager(repository)

    private val themePreference = ThemePreference.getInstance(application)
    val themeState = themePreference.themeFlow
    val themeMode: StateFlow<String> = themePreference.themeFlow

    fun toggleTheme(isCurrentlyDark: Boolean) {
        val newTheme = if (isCurrentlyDark) "light" else "dark"
        themePreference.setTheme(newTheme)
    }

    fun setThemeMode(mode: String) {
        themePreference.setTheme(mode)
    }

    // --- AUTH & PROFILE ---
    private val _isLoggedIn = MutableStateFlow(userPrefs.getBoolean("is_logged_in", false) || authManager.getCurrentUser() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _teacherName = MutableStateFlow(userPrefs.getString("teacher_name", authManager.getCurrentUser()?.displayName ?: "Abu Torab High School Teacher") ?: "Abu Torab High School Teacher")
    val teacherName: StateFlow<String> = _teacherName.asStateFlow()

    private val _schoolName = MutableStateFlow(userPrefs.getString("school_name", "Abu Torab High School") ?: "Abu Torab High School")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _userEmail = MutableStateFlow(userPrefs.getString("user_email", authManager.getCurrentUser()?.email ?: "teacher@abutorab.edu.bd") ?: "teacher@abutorab.edu.bd")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _showSyncConflictDialog = MutableStateFlow(false)
    val showSyncConflictDialog: StateFlow<Boolean> = _showSyncConflictDialog.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(context)
            result.onSuccess { user ->
                val email = user.email ?: ""
                val name = user.displayName ?: "Senior Teacher"
                userPrefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_email", email)
                    .putString("teacher_name", name)
                    .apply()
                _isLoggedIn.value = true
                _userEmail.value = email
                _teacherName.value = name

                // Restore this: decide push vs pull now that we have a uid
                val uid = user.uid
                val hasCloud = syncManager.hasCloudData(uid)
                val hasLocal = syncManager.hasLocalData()
                when {
                    !hasCloud && hasLocal -> syncManager.pushAllToCloud(uid)
                    hasCloud && !hasLocal -> syncManager.pullAllFromCloud(uid)
                    hasCloud && hasLocal -> { 
                        _showSyncConflictDialog.value = true
                    }
                }
            }.onFailure { e ->
                android.widget.Toast.makeText(context, "Sign-in failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun triggerManualSync() {
        val user = authManager.getCurrentUser() ?: return
        viewModelScope.launch {
            val uid = user.uid
            val hasCloud = syncManager.hasCloudData(uid)
            val hasLocal = syncManager.hasLocalData()
            when {
                !hasCloud && hasLocal -> syncManager.pushAllToCloud(uid)
                hasCloud && !hasLocal -> syncManager.pullAllFromCloud(uid)
                hasCloud && hasLocal -> { 
                    _showSyncConflictDialog.value = true
                }
            }
        }
    }

    fun resolveSyncConflict(keepLocal: Boolean) {
        val user = authManager.getCurrentUser()
        if (user != null) {
            viewModelScope.launch {
                val uid = user.uid
                if (keepLocal) {
                    syncManager.pushAllToCloud(uid)
                } else {
                    syncManager.pullAllFromCloud(uid)
                }
                _showSyncConflictDialog.value = false
            }
        } else {
            _showSyncConflictDialog.value = false
        }
    }

    fun dismissSyncConflict() {
        _showSyncConflictDialog.value = false
    }

    fun signOut(context: Context? = null) {
        if (context != null) {
            viewModelScope.launch {
                authManager.signOut(context)
            }
        }
        userPrefs.edit().putBoolean("is_logged_in", false).apply()
        _isLoggedIn.value = false
    }

    fun updateTeacherProfile(name: String, school: String) {
        userPrefs.edit()
            .putString("teacher_name", name)
            .putString("school_name", school)
            .apply()
        _teacherName.value = name
        _schoolName.value = school
    }

    private val _geminiApiKey = MutableStateFlow(userPrefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun saveGeminiApiKey(key: String) {
        userPrefs.edit().putString("gemini_api_key", key).apply()
        _geminiApiKey.value = key
    }

    // --- GLOBAL SCOPE STATE ---
    private val _selectedYearInt = MutableStateFlow(2026)
    val selectedYear: StateFlow<String> = _selectedYearInt.map { it.toString() }.stateIn(viewModelScope, SharingStarted.Lazily, "2026")
    val selectedYearInt: StateFlow<Int> = _selectedYearInt.asStateFlow()

    private val _selectedTerm = MutableStateFlow("ARDHOBARSHIK")
    val selectedTerm: StateFlow<String> = _selectedTerm.asStateFlow()

    fun selectYear(year: String) {
        _selectedYearInt.value = year.toIntOrNull() ?: 2026
    }

    fun selectYear(year: Int) {
        _selectedYearInt.value = year
    }

    fun selectTerm(term: String) {
        _selectedTerm.value = term
    }

    fun setYearAndTerm(year: Int, term: String) {
        _selectedYearInt.value = year
        _selectedTerm.value = term
    }

    // Global
    val allSubjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allStudents = _selectedYearInt.flatMapLatest { year ->
        repository.getStudentsByYear(year)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val ardhoMarks = _selectedYearInt.flatMapLatest { year ->
        repository.getAllMarks(year, "ARDHOBARSHIK")
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val barshikMarks = _selectedYearInt.flatMapLatest { year ->
        repository.getAllMarks(year, "BARSHIK")
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allMarks = combine(_selectedYearInt, _selectedTerm, _ardhoPercent, ardhoMarks, barshikMarks) { year, term, ardhoPct, ardhoList, barshikList ->
        Triple(Triple(year, term, ardhoPct), ardhoList, barshikList)
    }.flatMapLatest { (triple, ardhoList, barshikList) ->
        val (year, term, ardhoPct) = triple
        if (term == "SOMONNITO") {
            combine(allStudents, allSubjects) { students, subjectsList ->
                val combinedMarks = mutableListOf<MarkEntity>()
                for (student in students) {
                    for (subj in subjectsList) {
                        val computed = CalculationUtils.getSomonnitoMarks(
                            year = year,
                            rollNumber = student.rollNumber,
                            subjectId = subj.id,
                            ardhoPercent = ardhoPct,
                            ardhoMarks = ardhoList,
                            barshikMarks = barshikList
                        )
                        if (computed != null) {
                            combinedMarks.add(computed)
                        }
                    }
                }
                combinedMarks
            }
        } else if (term == "ARDHOBARSHIK") {
            flowOf(ardhoList)
        } else {
            flowOf(barshikList)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repository.createInitialDataIfEmpty()
            repository.ensurePredefinedSubjectsExist()

            // Auto-fetch from cloud if logged in so user data is never lost across sessions/reloads
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                val uid = currentUser.uid
                try {
                    val hasCloud = syncManager.hasCloudData(uid)
                    if (hasCloud) {
                        syncManager.pullAllFromCloud(uid)
                    } else if (syncManager.hasLocalData()) {
                        syncManager.pushAllToCloud(uid)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- STUDENTS DIRECTORY ---
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    val filteredStudents = combine(allStudents, _studentSearchQuery) { students, query ->
        if (query.isBlank()) {
            students
        } else {
            students.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.rollNumber.toString().contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onStudentSearchChanged(query: String) {
        _studentSearchQuery.value = query
    }

    fun addStudent(rollNumber: Int, name: String) {
        viewModelScope.launch {
            val student = StudentEntity(rollNumber = rollNumber, name = name, year = _selectedYearInt.value)
            repository.insertStudent(student)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushSingleChange(uid, "students", "${student.year}_${student.rollNumber}", student)
            }
        }
    }

    fun importStudentsFromCsv(csvData: String) {
        viewModelScope.launch {
            val lines = csvData.lines()
            val studentsToImport = mutableListOf<StudentEntity>()
            val currentYear = _selectedYearInt.value
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split(",")
                if (parts.size >= 2) {
                    val roll = parts[0].trim().toIntOrNull()
                    val name = parts[1].trim()
                    if (roll != null && name.isNotBlank()) {
                        val section = if (parts.size >= 3) parts[2].trim() else "A"
                        val shift = if (parts.size >= 4) parts[3].trim() else "Morning"
                        studentsToImport.add(StudentEntity(rollNumber = roll, name = name, year = currentYear))
                    }
                }
            }
            if (studentsToImport.isNotEmpty()) {
                repository.insertStudents(studentsToImport)
                authManager.getCurrentUser()?.uid?.let { uid ->
                    syncManager.pushAllToCloud(uid)
                }
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushSingleChange(uid, "students", "${student.year}_${student.rollNumber}", student)
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.deleteSingleDocument(uid, "students", "${student.year}_${student.rollNumber}")
            }
        }
    }

    fun addSubject(
        id: String,
        title: String,
        maxMarks: Int,
        passMarks: Int,
        hasMcq: Boolean,
        maxMcq: Int,
        hasWritten: Boolean,
        maxWritten: Int,
        hasPractical: Boolean,
        maxPractical: Int
    ) {
        viewModelScope.launch {
            val subject = SubjectEntity(
                id = id,
                title = title,
                maxMarks = maxMarks,
                passMarks = passMarks,
                hasMcq = hasMcq,
                maxMcq = maxMcq,
                hasWritten = hasWritten,
                maxWritten = maxWritten,
                hasPractical = hasPractical,
                maxPractical = maxPractical
            )
            repository.insertSubject(subject)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushSingleChange(uid, "subjects", subject.id, subject)
            }
        }
    }

    fun addSubject(
        title: String,
        code: String,
        maxMcq: Int,
        maxWritten: Int,
        maxPractical: Int
    ) {
        viewModelScope.launch {
            val id = "sub_" + System.currentTimeMillis()
            val hasMcq = maxMcq > 0
            val hasWritten = maxWritten > 0
            val hasPractical = maxPractical > 0
            val maxMarks = (if (hasMcq) maxMcq else 0) + (if (hasWritten) maxWritten else 0) + (if (hasPractical) maxPractical else 0)
            val subject = SubjectEntity(
                id = id,
                title = title,
                maxMarks = if (maxMarks > 0) maxMarks else 100,
                passMarks = 33,
                hasMcq = hasMcq,
                maxMcq = maxMcq,
                hasWritten = hasWritten,
                maxWritten = maxWritten,
                hasPractical = hasPractical,
                maxPractical = maxPractical
            )
            repository.insertSubject(subject)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushSingleChange(uid, "subjects", subject.id, subject)
            }
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushSingleChange(uid, "subjects", subject.id, subject)
            }
        }
    }

    fun deleteSubject(subject: SubjectEntity, replaceWithId: String? = null) {
        viewModelScope.launch {
            repository.deleteSubjectAndTransferMarks(subject, replaceWithId)
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.deleteSingleDocument(uid, "subjects", subject.id)
            }
        }
    }

    fun resetAllSubjectsToDefault() {
        viewModelScope.launch {
            repository.ensurePredefinedSubjectsExist()
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushAllToCloud(uid)
            }
        }
    }

    fun loadPredefinedSubjects() {
        viewModelScope.launch {
            repository.ensurePredefinedSubjectsExist()
            authManager.getCurrentUser()?.uid?.let { uid ->
                syncManager.pushAllToCloud(uid)
            }
        }
    }

    // --- QUICK EDIT STATE ---
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId = _selectedSubjectId.asStateFlow()

    val selectedSubject = combine(_selectedSubjectId, allSubjects) { id, subjects ->
        if (id == null) subjects.firstOrNull() else subjects.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeQuickEditData = combine(selectedSubject, _selectedYearInt, _selectedTerm, ::Triple)
        .flatMapLatest { (subject, year, term) ->
            if (subject == null) {
                flowOf(emptyList())
            } else {
                combine(allStudents, repository.getMarksForSubject(subject.id, year, term)) { students, marks ->
                    val markMap = marks.associateBy { it.rollNumber }
                    students.map { student ->
                        StudentWithMark(student, markMap[student.rollNumber])
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectSubject(subjectId: String) {
        _selectedSubjectId.value = subjectId
    }

    fun saveMark(rollNumber: Int, mcq: Int?, written: Int?, practical: Int?) {
        val subjectId = selectedSubject.value?.id ?: return
        val year = _selectedYearInt.value
        val term = _selectedTerm.value
        viewModelScope.launch {
            repository.saveMark(rollNumber, subjectId, mcq, written, practical, year, term)
            authManager.getCurrentUser()?.uid?.let { uid ->
                val mark = MarkEntity(
                    rollNumber = rollNumber,
                    subjectId = subjectId,
                    year = year,
                    term = term,
                    mcq = mcq,
                    written = written,
                    practical = practical
                )
                syncManager.pushSingleChange(uid, "marks", "${year}_${term}_${rollNumber}_${subjectId}", mark)
            }
        }
    }

    // --- TABULATION STATE ---
    val tabulationData = combine(allStudents, allMarks, allSubjects) { students, marks, subjects ->
        val marksByStudent = marks.groupBy { it.rollNumber }

        val tabulationRows = students.map { student ->
            val studentMarks = marksByStudent[student.rollNumber] ?: emptyList()
            val rawResults = studentMarks.associateBy { it.subjectId }

            // Build full results map ensuring every subject exists (even if missing/null)
            val results = subjects.associate { subj ->
                val mark = rawResults[subj.id]
                subj.id to SubjectResult(subj, mark?.mcq, mark?.written, mark?.practical)
            }

            var totalMarks = 0
            var totalPoints = 0.0
            var failedSubjectCount = 0
            var normalSubjectCount = 0

            val bangla1 = results.values.find { it.subject.title.contains("bangla", true) && it.subject.title.contains("1st", true) }
            val bangla2 = results.values.find { it.subject.title.contains("bangla", true) && it.subject.title.contains("2nd", true) }
            val english1 = results.values.find { it.subject.title.contains("english", true) && it.subject.title.contains("1st", true) }
            val english2 = results.values.find { it.subject.title.contains("english", true) && it.subject.title.contains("2nd", true) }

            val combinedSubjects = setOfNotNull(bangla1?.subject?.id, bangla2?.subject?.id, english1?.subject?.id, english2?.subject?.id)

            fun processCombined(paper1: SubjectResult?, paper2: SubjectResult?) {
                val p1Inputted = paper1?.let { it.mcq != null || it.written != null || it.practical != null } == true
                val p2Inputted = paper2?.let { it.mcq != null || it.written != null || it.practical != null } == true

                if (paper1 != null && paper2 != null) {
                    if (p1Inputted || p2Inputted) {
                        val total = (if (p1Inputted) paper1.total else 0) + (if (p2Inputted) paper2.total else 0)
                        totalMarks += total

                        val combinedGrade = calculateGrade(total, paper1.subject.maxMarks + paper2.subject.maxMarks)

                        if (combinedGrade.point == 0.0) {
                            failedSubjectCount++
                        }

                        totalPoints += combinedGrade.point
                        normalSubjectCount++
                    }
                } else if (paper1 != null && p1Inputted) {
                    totalMarks += paper1.total
                    if (paper1.grade.point == 0.0) failedSubjectCount++
                    totalPoints += paper1.grade.point
                    normalSubjectCount++
                } else if (paper2 != null && p2Inputted) {
                    totalMarks += paper2.total
                    if (paper2.grade.point == 0.0) failedSubjectCount++
                    totalPoints += paper2.grade.point
                    normalSubjectCount++
                }
            }

            processCombined(bangla1, bangla2)
            processCombined(english1, english2)

            results.values.forEach { result ->
                if (!combinedSubjects.contains(result.subject.id)) {
                    val isInputted = result.mcq != null || result.written != null || result.practical != null
                    if (isInputted) {
                        totalMarks += result.total

                        val grade = result.grade
                        if (grade.point == 0.0) {
                            failedSubjectCount++
                        }

                        totalPoints += grade.point
                        normalSubjectCount++
                    }
                }
            }

            var finalGpa = 0.0
            var finalGrade = if (normalSubjectCount == 0) "-" else "F"

            val hasFail = failedSubjectCount > 0

            if (!hasFail && normalSubjectCount > 0) {
                finalGpa = totalPoints / normalSubjectCount
                if (finalGpa > 5.0) finalGpa = 5.0

                // Format up to 2 decimal places logically for the step comparison
                val gpaRounded = (round(finalGpa * 100) / 100.0)

                finalGrade = when {
                    gpaRounded >= 5.0 -> "A+"
                    gpaRounded >= 4.0 -> "A"
                    gpaRounded >= 3.5 -> "A-"
                    gpaRounded >= 3.0 -> "B"
                    gpaRounded >= 2.0 -> "C"
                    gpaRounded >= 1.0 -> "D"
                    else -> "F"
                }
            }

            TabulationRow(
                student = student,
                results = results,
                totalMarks = totalMarks,
                finalGpa = round(finalGpa * 100) / 100.0,
                finalGrade = finalGrade,
                failedSubjectCount = failedSubjectCount
            )
        }

        // Calculate merit position
        val sortedTabulation = tabulationRows.sortedWith(
            compareBy<TabulationRow> { if (it.finalGrade == "-") 1 else 0 } // Move unevaluated to the bottom
                .thenBy { it.failedSubjectCount }
                .thenByDescending { it.totalMarks }
        )

        sortedTabulation.mapIndexed { index, row ->
            row.copy(meritPosition = index + 1)
        }.sortedBy { it.student.rollNumber } // Return sorted by roll number for the grid
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- MARKSHEET STATE ---
    private val _marksheetSearchQuery = MutableStateFlow("")
    val marksheetSearchQuery = _marksheetSearchQuery.asStateFlow()

    val searchedMarksheet = combine(tabulationData, _marksheetSearchQuery) { tabulation, query ->
        val queryRoll = query.toIntOrNull() ?: return@combine null
        tabulation.find { it.student.rollNumber == queryRoll }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onMarksheetSearchChanged(query: String) {
        _marksheetSearchQuery.value = query
    }
}
