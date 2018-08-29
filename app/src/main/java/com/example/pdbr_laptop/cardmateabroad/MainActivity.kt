package com.example.pdbr_laptop.cardmateabroad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL




class MainActivity : AppCompatActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = ArrayAdapter.createFromResource(this, R.array.Currencies, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        localSpinner.adapter = adapter
        homeSpinner.adapter = adapter
        homeSpinner.setSelection(1)

        var bd = BankData("Handelsbanken",9F,1.016F)
        launch(UI) {
            bd = fetchBankData(bd)
        }
        launch (UI){
            calculateButton.setOnClickListener {
                if (bankFee.text.toString().toFloatOrNull() != null){
                    calculate(bd.rate, bankFee.text.toString().toFloat())
                }
                else {
                    Toast.makeText(this@MainActivity, "Invalid Bank Fee", Toast.LENGTH_SHORT).show()
                }
            }
            refreshButton.setOnClickListener {
                launch (UI){
                    bd = fetchBankData(bd)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveBankFee()
        saveHomeCurrency()
        saveLocalCurrency()
    }

    override fun onResume() {
        super.onResume()
        retrieveBankFee()
        retrieveHomeCurrency()
        retrieveLocalCurrency()
    }
    @SuppressLint("SetTextI18n")
    private fun calculate(conversionRate: Float, conversionFee: Float){
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        //val fromCurr = localSpinner.selectedItem.toString()
        //val toCurr = homeSpinner.selectedItem.toString()
        val valueString = inputValue.text.toString()
        val value :Float
            value = if (valueString == ""){
                0F
            }
            else {
                (valueString).toFloat()
            }
        val result = df.format(value*conversionRate*(1+conversionFee/100))
        conversionInfo.text = "A purchase of: "+value.toString()+"\nwould cost you: " +result.toString()
    }

    private suspend fun fetchBankData(bankData: BankData):BankData {
        val fromCurr = localSpinner.selectedItem.toString()
        val toCurr = homeSpinner.selectedItem.toString()
        try {
            val url = async {
                URL("http://free.currencyconverterapi.com/api/v5/convert?q=" + fromCurr + "_" + toCurr + "&compact=y").readText(Charsets.UTF_8)
            }
            val reader = JSONObject(url.await())
            val currency = reader.getJSONObject(fromCurr +"_"+ toCurr)
            val rate = currency.getString("val").toFloat()
            bankData.rate = rate
        } catch (e: Exception){
            Toast.makeText(this@MainActivity, "Bank data could not be fetched", Toast.LENGTH_SHORT).show()
        }


        return bankData
    }
    private fun retrieveBankFee (){
        bankFee.setText(retrieveSavedValue("Bank Fee"))
    }
    private fun retrieveLocalCurrency (){
        val value = retrieveSavedValue("Local Currency").toIntOrNull()
        if (value != null) {
            localSpinner.setSelection(value)
        }
    }
    private fun retrieveHomeCurrency (){
        val value = retrieveSavedValue("Home Currency").toIntOrNull()
        if (value != null) {
            homeSpinner.setSelection(value)
        }
    }
    private fun retrieveSavedValue (filename:String) :String{
        var line = ""
        try {
            val file = InputStreamReader(openFileInput(filename))
            val br = BufferedReader(file)
            line = br.readLine()
            br.close()
            file.close()
            return line
        } catch (e : IOException){
        }
        return line
    }
    private fun saveBankFee(){
        saveSingleLine("Bank Fee",bankFee.text.toString())
    }
    private fun saveLocalCurrency (){
        saveSingleLine("Local Currency",localSpinner.selectedItemPosition.toString())
    }
    private fun saveHomeCurrency () {
        saveSingleLine("Home Currency",homeSpinner.selectedItemPosition.toString())
    }
    private fun saveSingleLine(name:String,value:String){
        try {
            val file = OutputStreamWriter(openFileOutput(name, Activity.MODE_PRIVATE))

            file.write (value)
            file.flush ()
            file.close ()
        } catch (e : IOException) {
        }
    }
}
