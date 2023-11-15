package com.example.prepmid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setContent {
            AppUI(viewModel = viewModel)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppUI(viewModel: UserViewModel) {
        viewModel.users.observe(this) {
            setContent {
                Column {
                    TopAppBar(
                        title = { Text(text = "User List") }
                    )

                    if (it.isNullOrEmpty()) {
                        Text(text = "Loading user...")
                    } else {
                        UserList(users = it)
                    }
                }
            }
        }
    }
}

class UserViewModel : ViewModel() {
    private val userService = RetrofitClient.userService

    private val _users = MutableLiveData<List<Users>>()
    val users: LiveData<List<Users>> get() = _users

    init {
        viewModelScope.launch {
            try {
                val response = userService.getUser()
                _users.value = response
            } catch (e: Exception) {
                Log.e("Retrofit", "Error fetching user data", e)
            }
        }
    }
}

@Composable
fun UserList(users: List<Users>) {
    LazyColumn {
        items(users) { user ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.website,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
}

data class Users(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: Address,
    val phone: String,
    val website: String,
    val company: Company
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo
)

data class Geo(
    val lat: String,
    val lng: String
)

data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)

interface UserService {
    @GET("users")
    suspend fun getUser(): List<Users>
}

