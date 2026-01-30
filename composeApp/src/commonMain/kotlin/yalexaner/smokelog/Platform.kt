package yalexaner.smokelog

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform