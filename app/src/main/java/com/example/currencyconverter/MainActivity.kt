package com.example.currencyconverter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import java.util.Date
import java.text.SimpleDateFormat
import kotlin.math.roundToLong


class MainActivity : AppCompatActivity() {
    //При отсутствии подключения к Интернету, данные на 12.06.2020
    // с сайта https://exchangeratesapi.io/
    var currencies: MutableMap<String, Double> = mutableMapOf(
        "EUR" to 1.0,
        "CAD" to 1.5347, "HKD" to 8.7607, "ISK" to 152.1,
        "PHP" to 56.763, "DKK" to 7.4551, "HUF" to 346.0, "CZK" to 26.698, "AUD" to 1.6447,
        "RON" to 4.8343, "SEK" to 10.5103, "IDR" to 16058.24, "INR" to 85.7875, "BRL" to 5.7388,
        "RUB" to 78.7662, "HRK" to 7.566, "JPY" to 121.26, "THB" to 34.986, "CHF" to 1.0697,
        "SGD" to 1.5718, "PLN" to 4.4484, "BGN" to 1.9558, "TRY" to 7.7224, "CNY" to 7.999,
        "NOK" to 10.8475, "NZD" to 1.7553, "ZAR" to 19.2794, "USD" to 1.1304, "MXN" to 25.445,
        "ILS" to 3.9189, "GBP" to 0.89653, "KRW" to 1360.45, "MYR" to 4.824
    )
    private var date: String = "2020-06-12"
    private var time = "-"


    override fun onCreate(savedInstanceState: Bundle?) {

        //проверка подключения к Интернету
        @Throws(InterruptedException::class, IOException::class)
        fun isConnected(): Boolean {
            val command = "ping -c 1 google.com"
            return Runtime.getRuntime().exec(command).waitFor() == 0
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //обновление курса (при входе в приложение)
        if (isConnected()) {
            try {
                Executors.newSingleThreadExecutor().execute {

                    val json: String = URL("https://api.exchangeratesapi.io/latest").readText()
                    //перевод данных в вид списка и перенос в переменные
                    val str: String = json.filterNot { (it in "{}\"\"") }
                    val str2: String = str.replace(":", ",")
                    val apiDat: List<String> = str2.split(",")
                    for (i in currencies.keys) {
                        if ((i != "EUR") and (i in apiDat)) {
                            currencies[i] = apiDat[apiDat.indexOf(i) + 1].toDouble()
                        }
                    }
                    date = apiDat[apiDat.indexOf("date") + 1]
                    //время обновления
                    val d = Date()
                    val format = SimpleDateFormat("dd.MM.yyyy в kk:mm")
                    time = format.format(d)
                    //вывод информации о полученных данных
                    dataInfo.post {
                        dataInfo.text = "Данные предоставлены " +
                                "https://api.exchangeratesapi.io на $date"
                    }
                    upInfo.post { upInfo.text = "Обновлено $time" }
                }
            } catch (e: Exception) {
                textView.text = "Ошибка при обновлении данных"
                dataInfo.post {
                    dataInfo.text = "Данные предоставлены https://api.exchangeratesapi.io" +
                            " на $date"
                }
            }
        } else {
            textView.text = "Нет подключения к интернету"
            dataInfo.post {
                dataInfo.text = "Данные предоставлены https://api.exchangeratesapi.io" +
                        " на $date"
            }}


            // адаптер
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item, currencies.keys.toTypedArray()
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner1.adapter = adapter
            spinner1.prompt = "Выберите валюту"
            spinner2.adapter = adapter
            spinner2.prompt = "Выберите валюту"
            //выбор валют по умолчанию
            spinner1.setSelection(27)
            spinner2.setSelection(14)

            editTextNumber1.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    val num1 = editTextNumber1.text.toString().toDoubleOrNull() //ввод суммы
                    if (num1 == null) {
                        editTextNumber2.text = "0"
                    } else {
                        val cur1 = spinner1.selectedItem.toString() //название валют
                        val cur2 = spinner2.selectedItem.toString()
                        //подсчет, округление до сотых
                        val num2 = if ((currencies[cur2] != null) and (currencies[cur1] != null))
                            ((currencies[cur2]!! * num1) / currencies[cur1]!!
                                    * 100).roundToLong().toDouble() / 100 else 0.0

                        editTextNumber2.text = "$num2"
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }
    }



