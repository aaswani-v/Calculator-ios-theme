package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.calculator.databinding.ActivityMainBinding
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    // View Binding: gives safe access to all views by their ID
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtons()
    }

    private fun setupButtons() {
        // Number buttons — each appends its digit to the input
        val numberButtons = mapOf(
            binding.button0 to "0", binding.button1 to "1",
            binding.button2 to "2", binding.button3 to "3",
            binding.button4 to "4", binding.button5 to "5",
            binding.button6 to "6", binding.button7 to "7",
            binding.button8 to "8", binding.button9 to "9"
        )
        numberButtons.forEach { (button, value) ->
            button.setOnClickListener {
                appendToInput(value)
                showResult()
            }
        }

        // Operator buttons
        binding.buttonPlus.setOnClickListener     { appendToInput("+");  showResult() }
        binding.buttonMinus.setOnClickListener    { appendToInput("−");  showResult() }
        binding.buttonMultiply.setOnClickListener { appendToInput("×");  showResult() }
        binding.buttonDivide.setOnClickListener   { appendToInput("÷");  showResult() }
        binding.buttonDot.setOnClickListener      { appendToInput(".");  showResult() }
        binding.buttonPercent.setOnClickListener  { appendToInput("%");  showResult() }

        // Smart bracket button: inserts ( or ) depending on how many are open
        binding.buttonBracket.setOnClickListener {
            val current = binding.input.text.toString()
            val openCount = current.count { it == '(' }
            val closeCount = current.count { it == ')' }
            if (openCount == closeCount || current.isEmpty()) appendToInput("(")
            else appendToInput(")")
            showResult()
        }

        // Delete: removes the last character typed
        binding.buttonDelete.setOnClickListener {
            val current = binding.input.text.toString()
            if (current.isNotEmpty()) {
                binding.input.text = current.dropLast(1)
                showResult()
            }
        }

        // Clear: wipes the screen
        binding.buttonClear.setOnClickListener {
            binding.input.text = ""
            binding.output.text = ""
        }

        // Equals: locks the result in as the new input
        binding.buttonEquals.setOnClickListener {
            val result = binding.output.text.toString()
            if (result.isNotEmpty() && result != "Can't ÷ by 0") {
                binding.input.text = result
                binding.output.text = ""
            }
        }
    }

    // Handles smart appending — auto-inserts × where mathematically implied
    // Example: typing "(" after "5" becomes "5×(" automatically
    private fun appendToInput(value: String) {
        val current = binding.input.text.toString()
        val newText = when {
            value == "(" && current.isNotEmpty() && current.last().isDigit() -> "$current×("
            value.first().isDigit() && current.isNotEmpty() && current.last() == ')' -> "$current×$value"
            else -> current + value
        }
        binding.input.text = newText
    }

    // Calculates and shows the live result below the input
    private fun showResult() {
        val rawInput = binding.input.text.toString()
        if (rawInput.isEmpty()) {
            binding.output.text = ""
            return
        }

        // mXparser needs standard math symbols, so swap our display symbols first
        val expression = rawInput
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")

        val result = Expression(expression).calculate()

        when {
            result.isNaN() -> {
                // Expression not complete yet (e.g. user just typed "5+")
                // Don't show error, just stay blank until they finish typing
                binding.output.text = ""
            }
            result.isInfinite() -> {
                // Division by zero
                binding.output.setTextColor(ContextCompat.getColor(this, R.color.error_red))
                binding.output.text = "Can't ÷ by 0"
            }
            else -> {
                // Valid result — format to max 6 decimal places, no trailing zeros
                binding.output.setTextColor(ContextCompat.getColor(this, R.color.text_main))
                binding.output.text = DecimalFormat("0.######").format(result)
            }
        }
    }
}