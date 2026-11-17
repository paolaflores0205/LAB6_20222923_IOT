package com.example.lab6;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;
import java.util.Map;

// Dibuja los valores ya calculados. Aquí no se consulta Firebase.
public class GraficoView extends View {
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] colores = {Color.rgb(37, 99, 235), Color.rgb(13, 148, 136), Color.rgb(234, 88, 12)};
    private ResumenEstudio datos = new ResumenEstudio();
    private boolean torta;
    private final int colorTexto;
    public GraficoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        android.content.res.TypedArray estilo = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        colorTexto = estilo.getColor(0, Color.DKGRAY);
        estilo.recycle();
    }
    private float dp(float n) { return n * getResources().getDisplayMetrics().density; }
    public void mostrar(ResumenEstudio resumen, boolean esTorta) {
        datos = resumen; torta = esTorta;
        StringBuilder descripcion = new StringBuilder(esTorta ? "Horas por actividad. " : "Horas por mes. ");
        if (esTorta) {
            for (int i = 0; i < 3; i++) descripcion.append(ResumenEstudio.TIPOS[i]).append(": ").append(datos.porTipo[i]).append(" horas. ");
        } else {
            for (Map.Entry<String, Double> mes : datos.porMes.entrySet()) descripcion.append(mes.getKey()).append(": ").append(mes.getValue()).append(" horas. ");
        }
        setContentDescription(descripcion.toString());
        requestLayout(); invalidate();
    }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int alto = (int) dp(torta ? 340 : Math.max(100, datos.porMes.size() * 46 + 25));
        setMeasuredDimension(resolveSize((int) dp(300), widthSpec), resolveSize(alto, heightSpec));
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datos.total == 0) { texto(canvas, "Sin datos para graficar", dp(12), dp(40)); return; }
        if (torta) dibujarTorta(canvas); else dibujarBarras(canvas);
    }
    private void texto(Canvas canvas, String texto, float x, float y) {
        pincel.setColor(colorTexto); pincel.setTextSize(dp(13));
        canvas.drawText(texto, x, y, pincel);
    }
    private String numero(double valor) { return String.format(new Locale("es", "PE"), "%.2f", valor); }
    private void dibujarBarras(Canvas canvas) {
        double maximo = 0;
        for (double valor : datos.porMes.values()) maximo = Math.max(maximo, valor);
        float inicio = dp(76), ancho = Math.max(dp(1), getWidth() - inicio - dp(85));
        int fila = 0;
        for (Map.Entry<String, Double> mes : datos.porMes.entrySet()) {
            float arriba = dp(12 + fila * 46);
            String clave = mes.getKey();
            texto(canvas, clave.substring(5) + "/" + clave.substring(0, 4), dp(4), arriba + dp(18));
            pincel.setColor(colores[0]);
            float largo = (float) (mes.getValue() / maximo * ancho);
            canvas.drawRect(inicio, arriba, inicio + largo, arriba + dp(26), pincel);
            texto(canvas, numero(mes.getValue()) + " h", inicio + largo + dp(6), arriba + dp(18));
            fila++;
        }
    }
    private void dibujarTorta(Canvas canvas) {
        float lado = Math.min(getWidth() - dp(24), dp(210));
        float izquierda = (getWidth() - lado) / 2;
        RectF circulo = new RectF(izquierda, dp(8), izquierda + lado, dp(8) + lado);
        float angulo = -90;
        for (int i = 0; i < 3; i++) {
            float porcion = (float) (datos.porTipo[i] / datos.total * 360);
            pincel.setColor(colores[i]);
            canvas.drawArc(circulo, angulo, porcion, true, pincel); angulo += porcion;
            float y = lado + dp(35 + i * 30);
            pincel.setColor(colores[i]);
            canvas.drawRect(dp(8), y - dp(12), dp(20), y, pincel);
            String etiqueta = ResumenEstudio.TIPOS[i] + ": " + numero(datos.porTipo[i]) + " h ("
                    + numero(datos.porTipo[i] / datos.total * 100) + "%)";
            texto(canvas, etiqueta, dp(28), y);
        }
    }
}
