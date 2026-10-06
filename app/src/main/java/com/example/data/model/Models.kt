package com.example.data.model

enum class Priority(val label: String, val colorHex: Long) {
    HIGH("Alta", 0xFFEF4444),
    MEDIUM("Media", 0xFFF59E0B),
    LOW("Baja", 0xFF10B981)
}

enum class TaskCategory(val label: String, val iconName: String) {
    PERSONAL("Personal", "person"),
    TRABAJO("Trabajo", "work"),
    ESTUDIO("Estudio", "school"),
    SALUD("Salud", "fitness_center"),
    PROYECTOS("Proyectos", "rocket_launch")
}

data class TaskItem(
    val id: String,
    val title: String,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val dateLabel: String = "Hoy"
)

data class HabitItem(
    val id: String,
    val name: String,
    val iconName: String = "water_drop",
    val completedDaysThisWeek: Int = 3,
    val targetDaysPerWeek: Int = 7,
    val completedToday: Boolean = false,
    val streak: Int = 5,
    val colorHex: Long = 0xFF4F46E5
)

enum class ExpenseCategory(val label: String, val iconName: String) {
    COMIDA("Comida", "restaurant"),
    TRANSPORTE("Transporte", "directions_car"),
    SERVICIOS("Servicios", "bolt"),
    OCIO("Ocio & Salidas", "movie"),
    SALUD("Salud", "medical_services"),
    TRABAJO("Ingresos/Salario", "payments"),
    OTROS("Otros", "category")
}

data class TransactionItem(
    val id: String,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: ExpenseCategory,
    val dateLabel: String = "Hoy"
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val colorHex: Long = 0xFFFEF3C7, // Soft warm amber
    val isPinned: Boolean = false,
    val dateLabel: String = "Hoy"
)

data class AppIdeaTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconCategory: String,
    val description: String,
    val features: List<String>,
    val audience: String,
    val difficulty: String
)

enum class AppNavDestination(val label: String, val title: String) {
    HOME("Inicio", "Mi Espacio"),
    TASKS("Tareas", "Mis Tareas"),
    HABITS("Hábitos", "Mis Hábitos"),
    FINANCES("Finanzas", "Billetera & Gastos"),
    NOTES("Notas", "Notas Rápidas"),
    AI_STUDIO("Gemini IA", "Estudio Gemini & Multimedia"),
    CREATOR("Taller", "Taller de Apps")
}
