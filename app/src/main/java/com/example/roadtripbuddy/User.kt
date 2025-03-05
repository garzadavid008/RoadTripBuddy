package com.example.roadtripbuddy

// User class
class User(var name: String ="",
            var email: String= "",
            var vehicle: String = "",
                                ) {
    // function for mapping kep value pairs ex ) name -> name
    // this will help when adding users to the db
fun toMap(): Map<String,Any>
    {
        return mapOf(
            "name" to name,
            "email" to email, // can add more here
            "vehicle" to vehicle,
        )
    }
fun getData(): List<String> {
    return listOf(name,email,vehicle)
}
    //

}