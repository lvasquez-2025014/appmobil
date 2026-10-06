package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ExpenseCategory
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

data class AiCreationRecord(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val prompt: String = "",
    val result: String = "",
    val modelUsed: String = "",
    val createdAtLabel: String = "Hoy"
)

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // TASKS
    fun observeTasks(userId: String): Flow<List<TaskItem>> {
        return db.collection("users").document(userId).collection("tasks")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val categoryStr = doc.getString("category") ?: TaskCategory.PERSONAL.name
                    val priorityStr = doc.getString("priority") ?: Priority.MEDIUM.name
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val dateLabel = doc.getString("dateLabel") ?: "Hoy"

                    TaskItem(
                        id = doc.id,
                        title = title,
                        category = runCatching { TaskCategory.valueOf(categoryStr) }.getOrDefault(TaskCategory.PERSONAL),
                        priority = runCatching { Priority.valueOf(priorityStr) }.getOrDefault(Priority.MEDIUM),
                        isCompleted = isCompleted,
                        dateLabel = dateLabel
                    )
                }
            }
    }

    suspend fun saveTask(userId: String, task: TaskItem) {
        val data = mapOf(
            "title" to task.title,
            "category" to task.category.name,
            "priority" to task.priority.name,
            "isCompleted" to task.isCompleted,
            "dateLabel" to task.dateLabel,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("tasks").document(task.id)
            .set(data)
            .await()
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        db.collection("users").document(userId).collection("tasks").document(taskId)
            .delete()
            .await()
    }

    // HABITS
    fun observeHabits(userId: String): Flow<List<HabitItem>> {
        return db.collection("users").document(userId).collection("habits")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val targetDays = doc.getLong("targetDaysPerWeek")?.toInt() ?: 7
                    val completedDays = doc.getLong("completedDaysThisWeek")?.toInt() ?: 0
                    val completedToday = doc.getBoolean("completedToday") ?: false
                    val streak = doc.getLong("streak")?.toInt() ?: 0
                    val colorHex = doc.getLong("colorHex") ?: 0xFF4F46E5

                    HabitItem(
                        id = doc.id,
                        name = name,
                        targetDaysPerWeek = targetDays,
                        completedDaysThisWeek = completedDays,
                        completedToday = completedToday,
                        streak = streak,
                        colorHex = colorHex
                    )
                }
            }
    }

    suspend fun saveHabit(userId: String, habit: HabitItem) {
        val data = mapOf(
            "name" to habit.name,
            "targetDaysPerWeek" to habit.targetDaysPerWeek,
            "completedDaysThisWeek" to habit.completedDaysThisWeek,
            "completedToday" to habit.completedToday,
            "streak" to habit.streak,
            "colorHex" to habit.colorHex,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("habits").document(habit.id)
            .set(data)
            .await()
    }

    // TRANSACTIONS
    fun observeTransactions(userId: String): Flow<List<TransactionItem>> {
        return db.collection("users").document(userId).collection("transactions")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val amount = doc.getDouble("amount") ?: 0.0
                    val isIncome = doc.getBoolean("isIncome") ?: false
                    val categoryStr = doc.getString("category") ?: ExpenseCategory.OTROS.name
                    val dateLabel = doc.getString("dateLabel") ?: "Hoy"

                    TransactionItem(
                        id = doc.id,
                        title = title,
                        amount = amount,
                        isIncome = isIncome,
                        category = runCatching { ExpenseCategory.valueOf(categoryStr) }.getOrDefault(ExpenseCategory.OTROS),
                        dateLabel = dateLabel
                    )
                }
            }
    }

    suspend fun saveTransaction(userId: String, tx: TransactionItem) {
        val data = mapOf(
            "title" to tx.title,
            "amount" to tx.amount,
            "isIncome" to tx.isIncome,
            "category" to tx.category.name,
            "dateLabel" to tx.dateLabel,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("transactions").document(tx.id)
            .set(data)
            .await()
    }

    suspend fun deleteTransaction(userId: String, txId: String) {
        db.collection("users").document(userId).collection("transactions").document(txId)
            .delete()
            .await()
    }

    // NOTES
    fun observeNotes(userId: String): Flow<List<NoteItem>> {
        return db.collection("users").document(userId).collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val colorHex = doc.getLong("colorHex") ?: 0xFFFEF3C7
                    val isPinned = doc.getBoolean("isPinned") ?: false
                    val dateLabel = doc.getString("dateLabel") ?: "Hoy"

                    NoteItem(
                        id = doc.id,
                        title = title,
                        content = content,
                        colorHex = colorHex,
                        isPinned = isPinned,
                        dateLabel = dateLabel
                    )
                }
            }
    }

    suspend fun saveNote(userId: String, note: NoteItem) {
        val data = mapOf(
            "title" to note.title,
            "content" to note.content,
            "colorHex" to note.colorHex,
            "isPinned" to note.isPinned,
            "dateLabel" to note.dateLabel,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("notes").document(note.id)
            .set(data)
            .await()
    }

    suspend fun deleteNote(userId: String, noteId: String) {
        db.collection("users").document(userId).collection("notes").document(noteId)
            .delete()
            .await()
    }

    // AI CREATIONS
    fun observeAiCreations(userId: String): Flow<List<AiCreationRecord>> {
        return db.collection("users").document(userId).collection("ai_creations")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    AiCreationRecord(
                        id = doc.id,
                        type = doc.getString("type") ?: "Chat",
                        title = doc.getString("title") ?: "Creación",
                        prompt = doc.getString("prompt") ?: "",
                        result = doc.getString("result") ?: "",
                        modelUsed = doc.getString("modelUsed") ?: "",
                        createdAtLabel = doc.getString("createdAtLabel") ?: "Hoy"
                    )
                }
            }
    }

    suspend fun saveAiCreation(userId: String, item: AiCreationRecord) {
        val data = mapOf(
            "type" to item.type,
            "title" to item.title,
            "prompt" to item.prompt,
            "result" to item.result,
            "modelUsed" to item.modelUsed,
            "createdAtLabel" to item.createdAtLabel,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("ai_creations").document(item.id)
            .set(data)
            .await()
    }
}
