package itstep.learning.andrpv221;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalcActivity extends AppCompatActivity {
    private int currentDigits = 0;
    private final int numOfDisplayDigits = 8;
    private final String[] operators = new String[] { "\u002B", "\u2212", "\u00D7", "\u00F7" };

    private TextView tvResult;
    private TextView tvHistory;

    private String zeroSign;

    private boolean buttonEqualWasClicked = false; // true if the last operation was '='
    private boolean operatorWasClicked = false; // true if the last button clicked was an operator
    private boolean digitWasClicked = false; // true if the last button clicked was a digit
    private boolean advancedOperationWasСalculated = false; // true if the last operation was advanced

    DecimalFormat decimalFormat;
    DecimalFormat scientificFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        decimalFormat = new DecimalFormat("#.########");
        scientificFormat = new DecimalFormat("0.#####E0");

        zeroSign = getResources().getString( R.string.calc_btn_digit_0 );

        tvResult = findViewById( R.id.calc_equal );
        tvHistory = findViewById( R.id.calc_history );

        findViewById( R.id.calc_btn_c ).setOnClickListener( this::btnClick_C );
        findViewById( R.id.calc_btn_backspace ).setOnClickListener( this::btnClickBackspace );

        findViewById( R.id.calc_btn_equal ).setOnClickListener( this::btnClickEqual );

        findViewById( R.id.calc_btn_plus ).setOnClickListener( this::btnClickOperation );
        findViewById( R.id.calc_btn_minus ).setOnClickListener( this::btnClickOperation );
        findViewById( R.id.calc_btn_mult ).setOnClickListener( this::btnClickOperation );
        findViewById( R.id.calc_btn_divide ).setOnClickListener( this::btnClickOperation );

        findViewById( R.id.calc_btn_square ).setOnClickListener( this::btnClickSquare );
        findViewById( R.id.calc_btn_sqrt ).setOnClickListener( this::btnClickSqrt );
        findViewById( R.id.calc_btn_inverce ).setOnClickListener( this::btnClickInverse );

        findViewById( R.id.calc_btn_pm ).setOnClickListener( this::btnClickPM );

        for (int i = 0; i < 10; i++) {
            @SuppressLint("DiscouragedApi") int id = getResources()
                    .getIdentifier("calc_btn_digit_" + i, "id", getPackageName());
            findViewById( id ).setOnClickListener( this::btnClickDigit );
        }

        btnClick_C( null );
    }

    private void btnClickPM(View view) {
        String resText = tvResult.getText().toString().replace("\uA668", "0");

        if (resText.charAt(0) == '-') {
            resText = resText.substring(1);
        } else {
            resText = "-" + resText;
        }

        tvResult.setText( resText );
    }

    private void btnClickInverse(View view) {
        String resText = tvResult.getText().toString().replace("\uA668", "0");
        String histText = tvHistory.getText().toString().replace("\uA668", "0");

        String newHistText = "";
        if (histText.isEmpty()) {
            newHistText = "1/(" + resText + ")";
        } else if (histText.split(" ").length == 1) {
            newHistText = "1/(" + histText + ")";
        }
        else {
            newHistText = histText + "1/(" + resText + ")";
        }
        tvHistory.setText( newHistText );

        BigDecimal resultValue = calculateAdvancedOperation( "1/(" + resText + ")" );
        String result = valueToString( resultValue, true );
        tvResult.setText( result );
    }

    private void btnClickSquare(View view) {
        String resText = tvResult.getText().toString().replace("\uA668", "0");
        String histText = tvHistory.getText().toString().replace("\uA668", "0");

        String newHistText = "";
        if (histText.isEmpty()) {
            newHistText = "sqr(" + resText + ")";
        } else if (histText.split(" ").length == 1) {
            newHistText = "sqr(" + histText + ")";
        }
        else {
            newHistText = histText + "sqr(" + resText + ")";
        }
        tvHistory.setText( newHistText );

        BigDecimal resultValue = calculateAdvancedOperation( "sqr(" + resText + ")" );
        String result = valueToString( resultValue, true );
        tvResult.setText( result );
    }

    private void btnClickSqrt(View view){
        String resText = tvResult.getText().toString().replace("\uA668", "0");
        String histText = tvHistory.getText().toString().replace("\uA668", "0");

        if (resText.contains("-")) {
            tvResult.setText("Invalid val.");
            tvHistory.setText( "√(" + resText + ")" );
            return;
        }


        String newHistText = "";
        if (histText.isEmpty()) {
            newHistText = "√(" + resText + ")";
        } else if (histText.split(" ").length == 1) {
            newHistText = "√(" + histText + ")";
        }
        else {
            newHistText = histText + "√(" + resText + ")";
        }
        tvHistory.setText( newHistText );

        BigDecimal resultValue = calculateAdvancedOperation( "√(" + resText + ")" );
        String result = valueToString( resultValue, true );
        tvResult.setText( result );
    }

    @SuppressLint("SetTextI18n")
    private void btnClickEqual(View view){
        String resText = tvResult.getText().toString().replace("\uA668", "0");
        String histText = tvHistory.getText().toString().replace("\uA668", "0");
        String result = "";
        buttonEqualWasClicked = true;
        digitWasClicked = false;
        operatorWasClicked = false;

        String[] partsOfExpression = histText.split(" ");
        if (partsOfExpression.length == 4) {
            result = calculateBasicOperation(resText, partsOfExpression[1], partsOfExpression[2]);
            tvResult.setText( result );
            tvHistory.setText( resText + " " + partsOfExpression[1] + " " + partsOfExpression[2] + " =" );
        } else if(partsOfExpression.length == 3) {
            result = calculateBasicOperation(partsOfExpression[0], partsOfExpression[1], partsOfExpression[2]);
            tvResult.setText( result );
            tvHistory.setText( partsOfExpression[0] + " " + partsOfExpression[1] + " " + resText + " =" );
        }
        else if(partsOfExpression.length == 2) {
            result = calculateBasicOperation(partsOfExpression[0], partsOfExpression[1], resText);
            tvResult.setText( result );
            tvHistory.setText( partsOfExpression[0] + " " +  partsOfExpression[1] + " " + resText + " =" );
        } else {
            tvResult.setText( "Щось пішло не так в equal..." );
        }
    }

    private String calculateBasicOperation(String leftSide_, String operator_, String rightSide_ ) {
        BigDecimal leftSide;
        BigDecimal rightSide;
        BigDecimal finalResult = BigDecimal.valueOf(0);

        if(leftSide_.contains("√") || leftSide_.contains("sqr") || leftSide_.contains("1/")) {
            leftSide = calculateAdvancedOperation( leftSide_ );
        }
        else {
            try {
                leftSide = stringToValue( leftSide_ );
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            } finally {
                tvResult.setText( "Ліва частина - неприпустиме значення" );
            }
        }

        if(rightSide_.contains("√") || rightSide_.contains("sqr") || rightSide_.contains("1/")) {
            rightSide = calculateAdvancedOperation( rightSide_ ) ;
        }
        else {
            try {
                rightSide = stringToValue( rightSide_ );
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            } finally {
                tvResult.setText( "Права частина - неприпустиме значення" );
            }
        }

        switch (Objects.requireNonNull(operator_)) {
            case "\u002B": // +
                finalResult = leftSide.add(rightSide);
                break;
            case "\u2212": // −
                finalResult = leftSide.subtract(rightSide);
                break;
            case "\u00D7": // ×
                finalResult = leftSide.multiply(rightSide);
                break;
            case "\u00F7": // ÷
                if (rightSide.compareTo(BigDecimal.ZERO) != 0) {
                    finalResult = leftSide.divide(rightSide);
                } else {
                    return "На нуль ділити не можна";
                }
                break;
            default:
                return "Невідомий оператор";
        }

        digitWasClicked = false;
        return valueToString( finalResult );
    }

    private BigDecimal calculateAdvancedOperation( String expression ) {
        Pattern pattern = Pattern.compile("(√|sqr|1/)\\s*\\(([^()]+)\\)");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String operation = matcher.group(1);
            String value = Objects.requireNonNull(matcher.group(2)).trim();
            BigDecimal evaluatedValue = stringToValue(value);

            BigDecimal finalValue;
            assert operation != null;
            if (operation.startsWith("√")) {
                finalValue = BigDecimal.valueOf(Math.sqrt(evaluatedValue.doubleValue()));
            } else if (operation.startsWith("sqr")) {
                finalValue = BigDecimal.valueOf(Math.pow(evaluatedValue.doubleValue(), 2));
            } else if (operation.startsWith("1/")) {
                finalValue = BigDecimal.valueOf(1 / evaluatedValue.doubleValue());
            } else {
                throw new IllegalArgumentException("Невідома операція: " + operation);
            }

            String lastExpression = operation + "(" + valueToString(evaluatedValue) + ")";
            expression = expression.replace(lastExpression, String.valueOf(valueToString(finalValue)));
            matcher = pattern.matcher(expression);
        }
        BigDecimal out = stringToValue(expression);
        digitWasClicked = false;
        advancedOperationWasСalculated = true;
        return out;
    }

    private BigDecimal stringToValue(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Помилка парсингу значення: " + value, e);
        }
    }

    private String valueToString(BigDecimal number) {
        return valueToString(number, false);
    }

    private String valueToString(BigDecimal number, boolean forDisplaying) {
        String plainString = number.toPlainString();

        // Якщо число є цілим (наприклад, 3.0), повертаємо тільки цілу частину
        if (number.stripTrailingZeros().scale() <= 0 && plainString.length() <= numOfDisplayDigits) {
            return number.toBigInteger().toString();
        }

        if (plainString.length() > numOfDisplayDigits) {
            // Розбиваємо рядок на цілу та дробову частини
            String[] parts = plainString.split("\\.");
            String integerPart = parts[0];                              // Ціла частина
            String fractionalPart = parts.length > 1 ? parts[1] : "";   // Дробова частина

            int availableLength = numOfDisplayDigits;  // Загальна довжина, яку можемо використовувати

            // Якщо ціла частина більше, ніж доступна довжина, обрізаємо
            if (integerPart.length() > availableLength && forDisplaying) {
                return "Overload..";
            }

            // Оновлюємо доступну довжину для дробової частини
            availableLength = (Math.max((availableLength - integerPart.length() + 1), 0));

            if (fractionalPart.length() > availableLength) {
                // Зберігаємо дробову частину до доступної довжини
                String toKeep = fractionalPart.substring(0, availableLength);

                // Перевірка на округлення
                if (availableLength > 0 && fractionalPart.length() > availableLength) {
                    String nextDigit = fractionalPart.substring(availableLength, availableLength + 1); // Наступний знак
                    // Якщо наступний знак >= 5, округлюємо
                    if (Integer.parseInt(nextDigit) >= 5) {
                        // Округлюємо в більший бік
                        return new BigDecimal(integerPart + "." + toKeep)
                                .add(BigDecimal.valueOf(1).scaleByPowerOfTen(-availableLength))
                                .toPlainString();
                    }
                }

                return integerPart + "." + toKeep;
            }

            return integerPart + (fractionalPart.isEmpty() ? "" : "." + fractionalPart);
        }

        return plainString; // Якщо довжина в межах, повертаємо без змін
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void btnClickBackspace ( View view ) {
        String resText = tvResult.getText().toString();
        if( resText.length() > 1 ) {
            resText = resText.substring( 0, resText.length() - 1 );
            currentDigits--;
        } else {
            resText = zeroSign;
            currentDigits = 0;
        }
        tvResult.setText( resText );
    }

    private void btnClickDigit ( View view ) {
        String resText = tvResult.getText().toString();

        if( resText.equals( zeroSign ) || buttonEqualWasClicked ) {
            resText = "";
        }
        if ( buttonEqualWasClicked ) {
            btnClick_C( view );
        }
        if( operatorWasClicked ) {
            resText = "";

            operatorWasClicked = false;
            buttonEqualWasClicked = false;
        }

        resText += ((TextView) view).getText();
        digitWasClicked = true;

        if(currentDigits < numOfDisplayDigits) {
            currentDigits++;
            tvResult.setText( resText );
        }
    }

    @SuppressLint("SetTextI18n")
    private void btnClickOperation (View view ) {
        String resText = tvResult.getText().toString();
        String histText = tvHistory.getText().toString();

        if(digitWasClicked && !buttonEqualWasClicked && histText.length() > 2) {  // В історій вже є частина виразу, в результаті є число
                                                                                    // і треба порахувати і додати оператор до результату
            char secondLastChar = histText.charAt(histText.length() - 2);
            for (String operator : operators) {
                if (operator.equals(String.valueOf(secondLastChar))) {
                    String result = calculateBasicOperation(histText.split(" ")[0], operator, resText);
                    tvResult.setText(result);
                    tvHistory.setText(result + " " + ((TextView) view).getText() + " ");

                    operatorWasClicked = true;
                    digitWasClicked = false;

                    break;
                }
            }
            return;
        }

        if( operatorWasClicked ) {  // Якщо був натиснутий оператор і треба замінити його на новий
            histText = histText.substring( 0, histText.length() - 2) + ((TextView) view).getText() + " ";
            tvHistory.setText( histText );
            return;
        }
        if ( buttonEqualWasClicked ) { // Якщо був натиснутий = і треба додати в історію результат та оператор
            histText = resText + " " + ((TextView) view).getText() + " ";
            tvHistory.setText( histText );
            buttonEqualWasClicked = false;
            operatorWasClicked = true;
            return;
        }

        if ( advancedOperationWasСalculated ){ // Якщо був обчислений корінь, квадрат або обернене значення і треба додати
                                                // в історію оператор
            histText = histText + " " + ((TextView) view).getText() + " ";
            tvHistory.setText( histText );
            advancedOperationWasСalculated = false;
            operatorWasClicked = true;
            return;
        }

        if( !resText.equals( zeroSign ) ) {  // загальний випадок
            histText = histText + resText + " " + ((TextView) view).getText() + " ";
            tvHistory.setText( histText );
            currentDigits = 0;
            operatorWasClicked = true;
            digitWasClicked = false;
        }
    }

    private void btnClick_C ( View view ) {
        tvResult.setText( zeroSign );
        tvHistory.setText( "" );
        currentDigits = 0;
        buttonEqualWasClicked = false;
        operatorWasClicked = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString( "tvResult", tvResult.getText().toString() );
        outState.putString( "tvHistory", tvHistory.getText().toString() );
        outState.putInt( "currentDigits", currentDigits );
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText( savedInstanceState.getString( "tvResult" ) );
        tvHistory.setText( savedInstanceState.getString( "tvHistory" ) );
        currentDigits = savedInstanceState.getInt( "currentDigits" );
    }
}