package com.example.dicodingstory.views.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.dicodingstory.R

class CustomEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), View.OnTouchListener {

    private val clearButton: Drawable? by lazy {
        getDrawableCompat(R.drawable.ic_clear)
    }
    private val showPasswordIcon: Drawable by lazy {
        getDrawableCompat(R.drawable.ic_eye_open)
    }
    private val hidePasswordIcon: Drawable by lazy {
        getDrawableCompat(R.drawable.ic_eye_close)
    }

    private var isPasswordVisible = false

    init {
        setOnTouchListener(this)
        setupTextChangeListener()
    }

    private fun getDrawableCompat(@DrawableRes drawableResId: Int): Drawable {
        return requireNotNull(ContextCompat.getDrawable(context, drawableResId)) {
            "Drawable with ID $drawableResId not found"
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        handleEndIcon()
    }

    private fun setupTextChangeListener() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleEndIcon()
                validateInput()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun handleEndIcon() {
        when (id) {
            R.id.ed_login_password, R.id.ed_register_password -> showPasswordToggle()
            R.id.ed_register_name, R.id.ed_register_email, R.id.ed_login_email -> {
                if (text?.isNotEmpty() == true) {
                    setButtonDrawables(endOfTheText = clearButton)
                } else {
                    setButtonDrawables()
                }
            }

            else -> setButtonDrawables()
        }
    }

    private fun showPasswordToggle() {
        val endIcon = if (isPasswordVisible) hidePasswordIcon else showPasswordIcon
        setButtonDrawables(endOfTheText = endIcon)
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val endDrawable = compoundDrawables[2] ?: return false

        val isEndDrawableClicked = when (layoutDirection) {
            View.LAYOUT_DIRECTION_RTL -> event.x < endDrawable.intrinsicWidth.toFloat()
            else -> event.x > (width - paddingEnd - endDrawable.intrinsicWidth)
        }

        if (isEndDrawableClicked) {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> true
                MotionEvent.ACTION_UP -> {
                    handleEndIconClick()
                    true
                }

                else -> false
            }
        }
        return false
    }

    private fun handleEndIconClick() {
        when (id) {
            R.id.ed_login_password, R.id.ed_register_password -> togglePasswordVisibility()
            R.id.ed_register_name, R.id.ed_register_email, R.id.ed_login_email -> text?.clear()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        setSelection(text?.length ?: 0)
        showPasswordToggle()
    }

    private fun validateInput() {
        val inputText = text.toString().trim()

        when (id) {
            R.id.ed_login_password, R.id.ed_register_password -> {
                error = when {
                    inputText.isEmpty() -> context.getString(R.string.password_empty_error)
                    inputText.length < 8 -> context.getString(R.string.password_length_error)
                    !isPasswordStrong(inputText) -> context.getString(R.string.password_complexity_error)
                    else -> null
                }
            }

            R.id.ed_register_email, R.id.ed_login_email -> {
                error = when {
                    inputText.isEmpty() -> context.getString(R.string.email_empty_error)
                    !isValidEmail(inputText) -> context.getString(R.string.email_format_error)
                    else -> null
                }
            }

            R.id.ed_register_name -> {
                error = when {
                    inputText.isEmpty() -> context.getString(R.string.name_empty_error)
                    inputText.length < 3 -> context.getString(R.string.name_length_error)
                    else -> null
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }
}
