package com.example.simplecalculatorapp

// CORE IMPORTS
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
// FOR SQRT()
import kotlin.math.sqrt
// FOR JSON OPERATIONS
import com.google.gson.JsonParser
// FOR HTML GET OPERATION
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
// FOR ASYNC TASK TO ACCESS THE INTERNET
import java.util.concurrent.Executors
import java.util.concurrent.CompletableFuture



// START OF ACTIVITY
class MainActivity : AppCompatActivity() {
    private lateinit var calculator: Calculator
    private lateinit var editText: EditText
    private var currentInput = StringBuilder()
    private var lastdigits = 0
    private val operatorIDsMap = mapOf(
        "add" to R.id.add,
        "subtract" to R.id.subtract,
        "multiply" to R.id.multiply,
        "divide" to R.id.divide
    )
    private var memoryInput = StringBuilder("0")
    private var tempInput = StringBuilder()
    private lateinit var spinnerBase: Spinner
    private lateinit var spinnerTarget: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting window transitions
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            enterTransition = Slide()
            exitTransition = Slide()
        }

        // Setting content view from XML
        setContentView(R.layout.activity_main)


        // Initializing Calculator class object
        calculator = Calculator()

        // Initializing text editor
        editText = findViewById(R.id.editText)

        // Setting Operators to gray since you should not start a calculation with an operator
        setGrayOps("")

        // Populating spinners with currency values and initializing dropdown menus
        val currencyArray = arrayOf("AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
            "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTC", "BTN", "BWP",
            "BYN", "BYR", "BZD", "CAD", "CDF", "CHF", "CLF", "CLP", "CNY", "COP", "CRC", "CUC", "CUP", "CVE",
            "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GGP",
            "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "IMP",
            "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW",
            "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LTL", "LVL", "LYD", "MAD", "MDL", "MGA",
            "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO",
            "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD",
            "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLE", "SLL", "SOS", "SRD", "STD",
            "SVC", "SYP", "SZL", "THB", "TJS")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerBase = findViewById(R.id.spinnerBaseCurrency)
        spinnerTarget = findViewById(R.id.spinnerTargetCurrency)

        spinnerBase.adapter = adapter
        spinnerTarget.adapter = adapter


        // Setting default options base:EUR target:GBP
        spinnerBase.setSelection(46, true)
        spinnerTarget.setSelection(49, true)
    }


    // Method for Number Buttons(0,1,2,3,4,5,6,7,8,9)
    fun onDigitButtonClick(view: View) {
        val button = view as Button
        currentInput.append(button.text)
        editText.setText(currentInput.toString())
        lastdigits = 1
        setOps()
    }


    // Method for operator(+,-,x,/) buttons
    fun onOperatorButtonClick(view: View) {
        val button = view as Button
        // Checking if the currentInput is not empty and the last character is not an operator
        if (currentInput.isNotEmpty() && !isLastCharOperator() ) {
            currentInput.append(" ${button.text} ")
            editText.setText(currentInput.toString())
            lastdigits = 3
            // Setting operators to gray since an operator was just inserted
            setGrayOps(button.text.toString())
        }
    }


    // Helper function to check if the last character in currentInput is an operator
    // (since operators have the form " <op> " and end in space, we check for space in the final position
    private fun isLastCharOperator(): Boolean {
        val lastChar = currentInput.lastOrNull()
        return lastChar == ' '
    }


    // Method for equals(=) button
    fun onEqualButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            if (!isLastCharOperator() ) {
                val result = calculator.evaluateExpression(currentInput.toString())
                currentInput.clear()
                if (tempInput.isNotEmpty()){
                    val result2 = calculator.evaluateExpression("$tempInput * $result /100")
                    if (isDoubleEqualToInt(result2)) {
                        // Update the currentInput and EditText
                        currentInput = StringBuilder(result2.toInt().toString())
                        editText.setText(result2.toInt().toString())
                    } else {
                        // Update the currentInput and EditText
                        currentInput = StringBuilder(result2.toString())
                        editText.setText(result2.toString())
                    }
                    tempInput.clear()
                    if(result2.toString()=="NaN")currentInput.clear()
                }
                else{
                    if (isDoubleEqualToInt(result)) {
                        // Update the currentInput and EditText with the Int value
                        currentInput = StringBuilder(result.toInt().toString())
                        editText.setText(result.toInt().toString())
                    } else {
                        // Update the currentInput and EditText with the Double value
                        currentInput = StringBuilder(result.toString())
                        editText.setText(result.toString())
                    }
                    if(result.toString()=="NaN")currentInput.clear()
                }

            }
            else{
                Toast.makeText(this,"Syntax invalid - last digit should not be an operator", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Method for Clear(C) button
    fun onClearButtonClick(view: View) {
        currentInput.clear()
        tempInput.clear()
        editText.setText("0")
    }


    // Method for Negate(+/-) button
    fun onNegateButtonClick(view: View) {
        // Convert the current input to a numeric type (Double, assuming it's a decimal number)
        val currentInputValue = currentInput.toString().toDoubleOrNull()

        // Check if the conversion was successful
        if (currentInputValue != null) {
            // Perform the Negation operation
            val negative = -currentInputValue.toString().toDoubleOrNull()!!
            if (isDoubleEqualToInt(negative)) {
                // Update the currentInput and EditText
                currentInput = StringBuilder(negative.toInt().toString())
            } else {
                // Update the currentInput and EditText
                currentInput = StringBuilder(negative.toString())
            }
            editText.setText(currentInput)
        } else {
            // Handling the case where the conversion fails (e.g., currentInput is not a valid number)
            Toast.makeText(this,"Negating error - expression is NaN", Toast.LENGTH_LONG).show()
        }
    }


    // Helper function to check if the value given as Double is the same as Int (has no decimals)
    private fun isDoubleEqualToInt(value: Double): Boolean {
        return value.toInt().toDouble() == value
    }


    // Method for Backspace(⌫) button
    fun onBackspaceButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            // Remove the last character from currentInput
            for (i in 1..lastdigits) {
                currentInput = StringBuilder(currentInput.substring(0, currentInput.length - 1))
                editText.setText(currentInput)
            }

            // Checking if previous character is Operator to change value of lastdigits
            if (isLastCharOperator()){
                setGrayOps(currentInput[currentInput.length-2].toString())
                lastdigits = 3
            }
            else {
                setOps()
                lastdigits = 1
            }


        }
    }


    // Helper function to set operators background to a gray color
    private fun setGrayOps(op : String) {
        for ((opString, opResourceId) in operatorIDsMap) {
            if (opString!=op) {
                val view = findViewById<View>(opResourceId)
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            }
        }
    }


    // Helper function to set operators background to the original color
    private fun setOps() {
        for ((opString, opResourceId) in operatorIDsMap) {
            val view = findViewById<View>(opResourceId)
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        }
    }


    // Method for MemoryStore(MS) button
    fun onMSButtonClick(view: View) {
        val button = view as Button
        val result = calculator.evaluateExpression(currentInput.toString())
        if(result.toString()!="NaN"){
            memoryInput = StringBuilder(currentInput)
            currentInput.clear()
        }
        else{
            Toast.makeText(this,"Memory store error - expression is NaN", Toast.LENGTH_LONG).show()
        }
    }


    // Method for MemoryPlus(M+) button
    fun onMPlusButtonClick(view: View) {
        val button = view as Button
        val result = calculator.evaluateExpression(currentInput.toString())
        if(result.toString()!="NaN"){
            val result2 = calculator.evaluateExpression("$currentInput+$memoryInput")
            memoryInput = StringBuilder(result2.toString())
        }
        else{
            Toast.makeText(this,"Memory addition error - expression is NaN", Toast.LENGTH_LONG).show()
        }
    }


    // Method for MemoryMinus(M-) button
    fun onMMinusButtonClick(view: View) {
        val button = view as Button
        val result = calculator.evaluateExpression(currentInput.toString())
        if(result.toString()!="NaN"){
            val result2 = calculator.evaluateExpression("$memoryInput-$currentInput")
            memoryInput = StringBuilder(result2.toString())
        }
        else{
            Toast.makeText(this,"Memory addition error - expression is NaN", Toast.LENGTH_LONG).show()
        }
    }


    // Method for MemoryClear(MC) button
    fun onMCButtonClick(view: View) {
        memoryInput = StringBuilder("0")
    }


    // Method for MemoryRecall(MR) button
    fun onMRButtonClick(view: View) {
//        Toast.makeText(this,memoryInput.toString().toDoubleOrNull()!!.toString(), Toast.LENGTH_SHORT).show()
        val doubleValue = memoryInput.toString().toDoubleOrNull()!!
        if (isDoubleEqualToInt(doubleValue)) {
            // Update the currentInput and EditText
            editText.setText(doubleValue.toInt().toString())
        } else {
            // Update the currentInput and EditText
            editText.setText(memoryInput)
        }
        currentInput.clear()
    }


    // Method for 1/x button
    fun onDivide1xButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            if (!isLastCharOperator() ) {
                val result = calculator.evaluateExpression("1 / $currentInput")
                currentInput.clear()
                if (isDoubleEqualToInt(result)) {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(result.toInt().toString())
                    editText.setText(result.toInt().toString())
                } else {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(result.toString())
                    editText.setText(result.toString())
                }
                if(result.toString()=="NaN")currentInput.clear()
            }
            else{
                Toast.makeText(this,"1/x division - expression is NaN", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Method for SquareRoot(√x) button
    fun onSquarerootButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            if (!isLastCharOperator() ) {
                val result = calculator.evaluateExpression("$currentInput")
                val finalResult = sqrt(result)
                currentInput.clear()
                if (isDoubleEqualToInt(finalResult)) {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(finalResult.toInt().toString())
                    editText.setText(finalResult.toInt().toString())
                } else {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(finalResult.toString())
                    editText.setText(finalResult.toString())
                }
                if(finalResult.toString()=="NaN")currentInput.clear()
            }
            else{
                Toast.makeText(this,"Squareroot error - expression is NaN", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Method for x² button
    fun on2PowerButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            if (!isLastCharOperator() ) {
                val result = calculator.evaluateExpression("$currentInput * $currentInput")
                currentInput.clear()
                if (isDoubleEqualToInt(result)) {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(result.toInt().toString())
                    editText.setText(result.toInt().toString())
                } else {
                    // Update the currentInput and EditText
                    currentInput = StringBuilder(result.toString())
                    editText.setText(result.toString())
                }
                if(result.toString()=="NaN")currentInput.clear()
            }
            else{
                Toast.makeText(this,"2nd power error - expression is NaN", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Method for ClearEntry(CE) button
    fun onCEButtonClick(view: View) {
        currentInput.clear()
        editText.setText("0")
    }


    // Method for Percentage(%) button
    fun onPercentageButtonClick(view: View) {
        if (currentInput.isNotEmpty()) {
            if (!isLastCharOperator() ) {
                val result = calculator.evaluateExpression("$currentInput")
                currentInput.clear()
                if (isDoubleEqualToInt(result)) {
                    // Update the currentInput and EditText
                    tempInput = StringBuilder(result.toInt().toString())
                } else {
                    // Update the currentInput and EditText
                    tempInput = StringBuilder(result.toString())
                }
                if(result.toString()=="NaN")currentInput.clear()
            }
            else{
                Toast.makeText(this,"Percentage error - expression is NaN", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Method for CovertValue button
    fun onExchangeRateButtonClick(view: View) {
        val executor = Executors.newSingleThreadExecutor()

        val fetchExchangeRateTask = CompletableFuture.supplyAsync({
            // Perform network operation in the background
            val accessKey = "3ecc391ebc38a412c5027af7c7dcfe95"
            val baseCurrency = spinnerBase.selectedItem.toString()
            val targetCurrency = spinnerTarget.selectedItem.toString()

            val apiUrl = "http://data.fixer.io/api/latest?access_key=$accessKey&base=$baseCurrency&symbols=$targetCurrency"

            val url = URL(apiUrl)
            Thread {
                try {
                    val url = URL(apiUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = StringBuilder()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        runOnUiThread {
//                            Toast.makeText(this, "Result: ${response.toString()}", Toast.LENGTH_LONG).show()
                            parseApiResponse(response.toString())
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Failed to retrieve data. Response Code: $responseCode", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }, executor)
    }


    // Helper function to parse the response from fixer.io and update UI
    private fun parseApiResponse(result: String) {
        // Using Gson to parse the JSON response
        val jsonElement = JsonParser.parseString(result)

        if (jsonElement.isJsonObject) {
            val jsonObject = jsonElement.asJsonObject
            val success = jsonObject.get("success").asString
            if (success.toBoolean()) {
                val ratesObject = jsonObject.getAsJsonObject("rates")

                if (ratesObject.has(spinnerTarget.selectedItem.toString())) {
                    val rate = ratesObject.get(spinnerTarget.selectedItem.toString()).asDouble

                    if (currentInput.isNotEmpty()) {
                        if (!isLastCharOperator() ) {
                            val result = calculator.evaluateExpression("$currentInput * $rate")
                            currentInput.clear()
                            if (isDoubleEqualToInt(result)) {
                                // Update the currentInput and EditText
                                currentInput = StringBuilder(result.toInt().toString())
                                editText.setText(result.toInt().toString())
                            } else {
                                // Update the currentInput and EditText
                                currentInput = StringBuilder(result.toString())
                                editText.setText(result.toString())
                            }
                            if(result.toString()=="NaN")currentInput.clear()
                        }
                        else{
                            Toast.makeText(this,"Syntax invalid", Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "${spinnerTarget.selectedItem} rate not found in the response.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Conversion unavailible with free plan.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Invalid JSON response.", Toast.LENGTH_LONG).show()
        }
    }

    //END OF MAIN ACTIVITY
}

//EOF