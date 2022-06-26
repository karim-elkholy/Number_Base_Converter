package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

const val MAX_TARGET_BASE = 36L
const val MIN_TARGET_BASE = 0L

const val NUM_OF_DIGITS = 9
const val NUM_OF_LETTERS = 26

// Different Bases
const val DECIMAL_BASE = 10
const val A_DECIMAL_FORM = 10

fun decToLatin(int: Int): String {

    // Throw an error if the decimal number cannot be represented in a single latin character
    if (int !in MIN_TARGET_BASE..MAX_TARGET_BASE) {
        throw NumberFormatException("Cannot represent $int as latin character")
    }

    // Holds the latin character representations for decimal numbers 0-36
    val map = mutableMapOf<Int, Char>()

    // Add the latin character representations for digits 0-9
    for (i in 0..NUM_OF_DIGITS) map[i] = i.digitToChar()

    // Add the latin character representations for letters a-z
    for (i in 0 until NUM_OF_LETTERS) map[A_DECIMAL_FORM + i] = 'a' + i

    return map[int].toString()
}

fun latinToDec(c: Char): Int {

    // Convert the character to lower case
    val lowercaseC = c.lowercaseChar()

    // Throw an error if the decimal number cannot be represented in a single latin character
    if (lowercaseC !in '0'..'9' && lowercaseC !in 'a'..'z') {
        throw NumberFormatException("'$lowercaseC' is not a valid latin letter/digit.")
    }

    // Holds the decimal representations for characters 0-9 & a-z
    val map = mutableMapOf<Char, Int>()

    // Add the decimal representations for digits 0-9
    for (i in '0'..'9') map[i] = i.digitToInt()

    // Add the decimal representations for letters a-z
    for (i in 0 until NUM_OF_LETTERS) map['a' + i] = A_DECIMAL_FORM + i

    return map[c]!!
}

fun decToBase(decimalNum: String, targetBase: Int): String {

    // Return the source if the target is a decimal number
    if (targetBase == DECIMAL_BASE) { return decimalNum }

    // Split the incoming decimal number
    val decimalNumList = decimalNum.split(".")

    // Holds the converted number
    var convertedNumber = ""

    // Convert the decimal number & target base to big integers
    var integerPart = decimalNumList[0].toBigInteger()
    val targetBaseBigInt = targetBase.toBigInteger()

    while (true) {
        val quotient = integerPart / targetBaseBigInt
        val remainder = integerPart % targetBaseBigInt

        // Get the value of this number as a latin character
        convertedNumber = decToLatin(remainder.intValueExact()) + convertedNumber

        if (quotient == BigInteger.ZERO) break

        // Update decimal number
        integerPart = quotient
    }

    // If the decimal number was a fraction
    if (decimalNumList.size > 1) {
        // Add a dot sign to the converted number
        convertedNumber += "."

        // Get the fraction
        var fractionalPart = "0.${decimalNumList[1]}".toBigDecimal()

        // Get the target base as a decimal
        val targetBaseDecimal = targetBase.toBigDecimal()

        // Stop if 0 is not reached after 100 attempts
        val maxPrecision = 100

        // println("target base decimal: $targetBaseDecimal")
        // println("fractionalPart: $fractionalPart")
        // println("result: ${(fractionalPart * targetBaseDecimal)}")

        // Store the current attempts
        var currentAttempts = 0

        do {

            // Multiply the fraction by the base
            val result = fractionalPart * targetBaseDecimal

            // Get the integer only from the result
            val integerOnly = result.toBigInteger().intValueExact()

            // Add the newly calculated integer as a latin character
            convertedNumber += decToLatin(integerOnly)

            // Update the fractional part
            fractionalPart = result - integerOnly.toBigDecimal()

            // Increment the number of attempts
            currentAttempts += 1
        } while (fractionalPart.compareTo(BigDecimal.ZERO) != 0 && currentAttempts < maxPrecision)
    }

    return convertedNumber
}

fun baseToDec(sourceNumber: String, sourceBase: Int): String {

    // Return the source if it is already represented as a decimal number
    if (sourceBase == DECIMAL_BASE) { return sourceNumber }

    // Split the incoming number
    val sourceNumList = sourceNumber.split(".")

    // Holds the sum of the integer part
    var integerSum = BigInteger.valueOf(0)

    // Iterate through the integer part from right-to-left
    sourceNumList[0].reversed().forEachIndexed { index, nextChar ->

        // Get the value of the next latin character as a decimal integer
        val charValue = latinToDec(nextChar).toBigInteger()

        // Add the sum of this chunk
        integerSum += charValue * sourceBase.toBigInteger().pow(index)
    }

    // If this contains a fraction
    if (sourceNumList.size > 1) {

        // Get the fraction part
        var fractionSum = integerSum.toBigDecimal()

        // Iterate through the integer part from left-to-right
        sourceNumList[1].forEachIndexed { index, nextChar ->

            // Get the value of the next latin character as a decimal
            val charValue = latinToDec(nextChar).toBigDecimal()

            // Add the decimal value of this section
            fractionSum += charValue * (sourceBase.toBigDecimal().pow(-1 - index, MathContext.DECIMAL128))
        }

        // Add the fractional part
        return "$fractionSum"
    }

    return "$integerSum"
}

fun baseToBase(sourceNumber: String, sourceBase: Int, targetBase: Int): String {

    // Return the source if the bases are already the same
    if (sourceBase == targetBase) return sourceNumber

    // Convert the number to decimal
    val decimalNumber = baseToDec(sourceNumber, sourceBase)

    // Convert the decimal number to the desired base
    return decToBase(decimalNumber, targetBase)
}

fun roundFractionalPart(number: String, scale: Int): String {
    // Return if the number is not a fraction
    if ("." !in number) { return number }

    val (intPart, fractionPart) = number.split(".")

    // Trim the fraction to be the specified number of digits
    if (fractionPart.length > scale) return "$intPart.${fractionPart.substring(0 until scale)}"

    // Add necessary zeros to satisfy the specified number of digits
    return "$intPart.$fractionPart${"0".repeat(scale - fractionPart.length)}"
}

fun main() {

    while (true) {
        print("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")

        val (sourceBase, targetBase) = when (val input2 = readln()) {
            "/exit" -> break
            else -> input2.split(" ").map { it.toInt() }
        }

        while (true) {
            print("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
            val input = readln()
            if (input == "/back") break

            // Convert the number
            var conversionResult = baseToBase(input, sourceBase, targetBase)

            // Round it to 5 decimal places
            conversionResult = roundFractionalPart(conversionResult, 5)

            println("Conversion result: $conversionResult")
        }
    }
}
