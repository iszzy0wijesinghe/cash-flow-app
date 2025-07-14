package com.example.money.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.money.R

class CurrencySpinnerAdapter(
    context: Context,
    private val currencies: Array<String>,
    private val currencyIcons: Array<Int>
) : ArrayAdapter<String>(context, 0, currencies) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val view = recycledView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_currency_spinner, parent, false)

        val currencyIcon = view.findViewById<ImageView>(R.id.imageCurrency)
        val currencyText = view.findViewById<TextView>(R.id.textCurrency)

        currencyIcon.setImageResource(currencyIcons[position])
        currencyIcon.visibility = View.VISIBLE
        
        currencyText.text = currencies[position]

        return view
    }
} 