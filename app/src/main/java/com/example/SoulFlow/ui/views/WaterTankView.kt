package com.example.SoulFlow.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.example.SoulFlow.R
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom view that displays an animated water bottle that fills from bottom to top
 * with bubble effects
 */
class WaterTankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var animatedProgress: Float = 0f
    private var progressAnimator: ValueAnimator? = null

    // Paint objects
    private val bottlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = ContextCompat.getColor(context, R.color.text_hint)
    }

    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.primary)
    }

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        alpha = 40
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        alpha = 120
    }

    private val bubbleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.WHITE
        alpha = 180
    }

    // Gradient colors for water
    private var waterGradient: LinearGradient? = null

    // Wave animation
    private var waveOffset = 0f
    private var waveAnimator: ValueAnimator? = null

    // Bubble system
    private data class Bubble(
        var x: Float,
        var y: Float,
        var radius: Float,
        var speed: Float,
        var alpha: Int
    )

    private val bubbles = mutableListOf<Bubble>()
    private var bubbleAnimator: ValueAnimator? = null

    init {
        // Start animations
        startWaveAnimation()
        startBubbleAnimation()
        initializeBubbles()
    }

    private fun initializeBubbles() {
        bubbles.clear()
        for (i in 0..8) {
            bubbles.add(createRandomBubble())
        }
    }

    private fun createRandomBubble(): Bubble {
        val width = width.toFloat()
        val height = height.toFloat()
        
        return Bubble(
            x = Random.nextFloat() * width * 0.5f + width * 0.25f,
            y = Random.nextFloat() * height * 0.6f + height * 0.3f,
            radius = Random.nextFloat() * 6f + 3f,
            speed = Random.nextFloat() * 2f + 1f,
            alpha = Random.nextInt(80, 150)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Create gradient for water fill
        val waterStartColor = ContextCompat.getColor(context, R.color.accent_blue)
        val waterEndColor = ContextCompat.getColor(context, R.color.primary)
        
        waterGradient = LinearGradient(
            0f, h.toFloat(),
            0f, 0f,
            waterStartColor,
            waterEndColor,
            Shader.TileMode.CLAMP
        )
        waterPaint.shader = waterGradient
        
        // Reinitialize bubbles with new dimensions
        if (bubbles.isEmpty()) {
            initializeBubbles()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        
        // Define bottle dimensions (modern bottle shape)
        val bottleBodyLeft = width * 0.25f
        val bottleBodyRight = width * 0.75f
        val bottleBodyTop = height * 0.20f
        val bottleBodyBottom = height * 0.90f
        val bottleBodyHeight = bottleBodyBottom - bottleBodyTop
        
        // Neck dimensions
        val neckLeft = width * 0.38f
        val neckRight = width * 0.62f
        val neckTop = height * 0.08f
        val neckBottom = bottleBodyTop
        
        // Cap dimensions
        val capLeft = width * 0.35f
        val capRight = width * 0.65f
        val capTop = height * 0.03f
        val capBottom = neckTop
        
        // Calculate water level based on progress
        val waterHeight = bottleBodyHeight * (animatedProgress / 100f)
        val waterTop = bottleBodyBottom - waterHeight

        // Draw water fill with gradient (only in body)
        if (animatedProgress > 0) {
            val waterPath = Path()
            
            // Create rounded bottom for water
            val cornerRadius = 15f
            val waterRect = RectF(bottleBodyLeft, waterTop, bottleBodyRight, bottleBodyBottom)
            waterPath.addRoundRect(waterRect, cornerRadius, cornerRadius, Path.Direction.CW)
            
            canvas.save()
            canvas.clipPath(waterPath)
            canvas.drawRect(bottleBodyLeft, waterTop, bottleBodyRight, bottleBodyBottom, waterPaint)
            canvas.restore()

            // Draw bubbles inside water
            if (animatedProgress > 10) {
                drawBubbles(canvas, bottleBodyLeft, bottleBodyRight, waterTop, bottleBodyBottom)
            }

            // Draw wave effect on top of water
            if (animatedProgress < 100 && waterTop > bottleBodyTop) {
                drawWave(canvas, bottleBodyLeft, bottleBodyRight, waterTop)
            }
        }

        // Draw bottle body (rounded rectangle)
        val bottleBodyPath = Path()
        val bodyCornerRadius = 20f
        bottleBodyPath.addRoundRect(
            RectF(bottleBodyLeft, bottleBodyTop, bottleBodyRight, bottleBodyBottom),
            bodyCornerRadius,
            bodyCornerRadius,
            Path.Direction.CW
        )
        canvas.drawPath(bottleBodyPath, bottlePaint)

        // Draw bottle neck
        val neckPath = Path()
        neckPath.moveTo(neckLeft, neckBottom)
        neckPath.lineTo(neckLeft, neckTop + 10f)
        neckPath.quadTo(neckLeft, neckTop, neckLeft + 10f, neckTop)
        neckPath.lineTo(neckRight - 10f, neckTop)
        neckPath.quadTo(neckRight, neckTop, neckRight, neckTop + 10f)
        neckPath.lineTo(neckRight, neckBottom)
        canvas.drawPath(neckPath, bottlePaint)

        // Draw bottle cap (filled)
        val capPath = Path()
        val capCornerRadius = 8f
        capPath.addRoundRect(
            RectF(capLeft, capTop, capRight, capBottom),
            capCornerRadius,
            capCornerRadius,
            Path.Direction.CW
        )
        canvas.drawPath(capPath, capPaint)
        
        // Add cap details (lines)
        val capDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.WHITE
            alpha = 100
        }
        val capCenterY = (capTop + capBottom) / 2f
        canvas.drawLine(capLeft + 10f, capCenterY, capRight - 10f, capCenterY, capDetailPaint)

        // Draw percentage text in the center of bottle body
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = height * 0.12f
            color = if (animatedProgress > 40) {
                Color.WHITE
            } else {
                ContextCompat.getColor(context, R.color.text_primary)
            }
            isFakeBoldText = true
            setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        }
        
        val progressText = "${animatedProgress.toInt()}%"
        val textY = (bottleBodyTop + bottleBodyBottom) / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(progressText, width / 2f, textY, textPaint)
    }

    private fun drawBubbles(canvas: Canvas, left: Float, right: Float, waterTop: Float, waterBottom: Float) {
        for (bubble in bubbles) {
            // Only draw bubbles that are within the water area
            if (bubble.y > waterTop && bubble.y < waterBottom) {
                bubblePaint.alpha = bubble.alpha
                bubbleStrokePaint.alpha = bubble.alpha + 30
                
                // Draw bubble fill
                canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubblePaint)
                
                // Draw bubble outline
                canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubbleStrokePaint)
                
                // Draw highlight on bubble
                val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = Color.WHITE
                    alpha = bubble.alpha + 50
                }
                canvas.drawCircle(
                    bubble.x - bubble.radius * 0.3f,
                    bubble.y - bubble.radius * 0.3f,
                    bubble.radius * 0.4f,
                    highlightPaint
                )
            }
        }
    }

    private fun drawWave(canvas: Canvas, left: Float, right: Float, waterTop: Float) {
        val wavePath = Path()
        val waveWidth = right - left
        val waveHeight = 10f
        val waveCount = 4

        wavePath.moveTo(left, waterTop)

        for (i in 0..waveCount * 2) {
            val x = left + (waveWidth / (waveCount * 2)) * i
            val phase = (i * Math.PI / waveCount) + (waveOffset / 15.0)
            val y = waterTop + waveHeight * sin(phase).toFloat()
            
            if (i == 0) {
                wavePath.lineTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        wavePath.lineTo(right, waterTop + waveHeight * 2)
        wavePath.lineTo(left, waterTop + waveHeight * 2)
        wavePath.close()

        canvas.drawPath(wavePath, wavePaint)
    }

    private fun startWaveAnimation() {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                waveOffset = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startBubbleAnimation() {
        bubbleAnimator?.cancel()
        bubbleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 50
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateBubbles()
                invalidate()
            }
            start()
        }
    }

    private fun updateBubbles() {
        val height = height.toFloat()
        val width = width.toFloat()
        val bottleBodyLeft = width * 0.25f
        val bottleBodyRight = width * 0.75f
        val bottleBodyTop = height * 0.20f
        val bottleBodyBottom = height * 0.90f
        val waterHeight = (bottleBodyBottom - bottleBodyTop) * (animatedProgress / 100f)
        val waterTop = bottleBodyBottom - waterHeight

        for (bubble in bubbles) {
            // Move bubble upward
            bubble.y -= bubble.speed
            
            // Add slight horizontal movement
            bubble.x += sin(bubble.y / 20.0).toFloat() * 0.5f
            
            // Reset bubble if it reaches the top of water or goes out of bounds
            if (bubble.y < waterTop - bubble.radius || bubble.y < bottleBodyTop) {
                bubble.y = bottleBodyBottom - bubble.radius
                bubble.x = Random.nextFloat() * (bottleBodyRight - bottleBodyLeft - 40f) + bottleBodyLeft + 20f
                bubble.radius = Random.nextFloat() * 6f + 3f
                bubble.speed = Random.nextFloat() * 2f + 1f
                bubble.alpha = Random.nextInt(80, 150)
            }
            
            // Keep bubbles within bottle bounds
            if (bubble.x < bottleBodyLeft + bubble.radius) {
                bubble.x = bottleBodyLeft + bubble.radius
            }
            if (bubble.x > bottleBodyRight - bubble.radius) {
                bubble.x = bottleBodyRight - bubble.radius
            }
        }
    }

    /**
     * Set the progress with animation
     * @param newProgress Progress value between 0 and 100
     * @param animate Whether to animate the change
     */
    fun setProgress(newProgress: Float, animate: Boolean = true) {
        val clampedProgress = newProgress.coerceIn(0f, 100f)
        
        if (!animate) {
            progress = clampedProgress
            animatedProgress = clampedProgress
            invalidate()
            return
        }

        // Cancel any ongoing animation
        progressAnimator?.cancel()

        // Animate from current to new progress
        progressAnimator = ValueAnimator.ofFloat(animatedProgress, clampedProgress).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animatedProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
        
        progress = clampedProgress
    }

    /**
     * Get the current progress value
     */
    fun getProgress(): Float = progress

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
        waveAnimator?.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 300
        val desiredHeight = 400

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}
