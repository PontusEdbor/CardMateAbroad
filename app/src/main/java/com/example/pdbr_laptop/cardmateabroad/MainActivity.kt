package com.example.pdbr_laptop.cardmateabroad

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import org.json.JSONObject
import java.net.URL
import android.widget.Spinner




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
                calculate(bd.rate, bd.fee)
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
        var value :Long
            value = if (valueString == ""){
                0L
            }
            else {
                (valueString).toLong()
            }
        val result = df.format(value*conversionRate*conversionFee)
        conversionInfo.text = "A purchase of: $value \nwould cost you: $result"
    }

    @SuppressLint("SetTextI18n")
    private suspend fun fetchBankData(bankData: BankData):BankData {
        val name = bankData.name
        var fromCurr = localSpinner.selectedItem.toString()
        var toCurr = homeSpinner.selectedItem.toString()
        val url = async {
            URL("http://free.currencyconverterapi.com/api/v5/convert?q="+"$fromCurr"+"_"+"$toCurr"+"&compact=y").readText(Charsets.UTF_8)
        }
        val reader = JSONObject(url.await())
        val currency = reader.getJSONObject("$fromCurr"+"_"+"$toCurr")
        val rate = currency.getString("val").toFloat()

        bankData.rate = rate

        return bankData
    }
}
