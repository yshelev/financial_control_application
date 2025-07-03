import com.example.myapplication.dataClasses.ExportData
import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.Category
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object ExportDataSerializer : KSerializer<ExportData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ExportData") {
        element<String>("username")
        element<List<UserTransaction>>("transactions")
        element<List<Category>>("categories")
        element<List<Card>>("cards")
    }

    override fun serialize(encoder: Encoder, value: ExportData) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.username)
            encodeSerializableElement(descriptor, 1, ListSerializer(Category.serializer()), value.categories)
            encodeSerializableElement(descriptor, 2, ListSerializer(UserTransaction.serializer()), value.transactions)
            encodeSerializableElement(descriptor, 3, ListSerializer(Card.serializer()), value.cards)
        }
    }

    override fun deserialize(decoder: Decoder): ExportData {
        return decoder.decodeStructure(descriptor) {
            var username = ""
            var categories = emptyList<Category>()
            var transactions = emptyList<UserTransaction>()
            var cards = emptyList<Card>()

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> username = decodeStringElement(descriptor, 0)
                    1 -> categories = decodeSerializableElement(descriptor, 1, ListSerializer(Category.serializer()))
                    2 -> transactions = decodeSerializableElement(descriptor, 2, ListSerializer(UserTransaction.serializer()))
                    3 -> cards = decodeSerializableElement(descriptor, 3, ListSerializer(Card.serializer()))
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            ExportData(username, categories, transactions, cards)
        }
    }
}