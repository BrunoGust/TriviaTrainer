import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(val message: String, val isUser: Boolean) : Parcelable
