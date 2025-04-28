package com.example.roadtripbuddy.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object TripsSerializer : Serializer<Trips> {
    override val defaultValue: Trips = Trips.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Trips =
        try   { Trips.parseFrom(input) }
        catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", e)
        }

    override suspend fun writeTo(t: Trips, output: OutputStream) = t.writeTo(output)
}
