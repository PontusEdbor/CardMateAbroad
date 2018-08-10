package com.example.pdbr_laptop.cardmateabroad

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import org.json.JSONObject
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

        var bd = BankData("Handelsbanken",8.8F,1.016F)
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
    @SuppressLint("SetTextI18n")
    private fun calculate(conversionRate: Float, conversionFee: Float){
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val valueString = inputValue.text.toString()
        val value :Float
            value = if (valueString == ""){
                0F
            }
            else {
                (valueString).toFloat()
            }
        val result = df.format(value*conversionRate*(1+conversionFee/100))
        conversionInfo.text = "A purchase of: $value \nwould cost you: $result"
    }

    private suspend fun fetchBankData(bankData: BankData):BankData {
        val fromCurr = localSpinner.selectedItem.toString()
        val toCurr = homeSpinner.selectedItem.toString()
        val url = async {
            URL("http://free.currencyconverterapi.com/api/v5/convert?q="+ fromCurr +"_"+ toCurr +"&compact=y").readText(Charsets.UTF_8)
        }
        val reader = JSONObject(url.await())
        val currency = reader.getJSONObject(fromCurr +"_"+ toCurr)
        val rate = currency.getString("val").toFloat()

        bankData.rate = rate

        return bankData
    }
}
