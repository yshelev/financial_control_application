import java.io.Serializable

data class Card(
    val name: String,
    val maskedNumber: String,
    val date: String,
    val currency: String,
    val balance: Double
) : Serializable
