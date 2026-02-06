package com.ecogo.ui.views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import com.ecogo.data.MascotEmotion
import com.ecogo.data.MascotSize
import com.ecogo.data.Outfit
import kotlin.math.min

/**
 * MascotLionView - 小狮子吉祥物自定义View
 * 
 * 功能:
 * - 绘制小狮子基础形状(身体、头部、尾巴、五官)
 * - 根据 Outfit 动态渲染装备
 * - 支持动画: 呼吸、眨眼、点击跳跃、尾巴摆动
 * - 支持 11 种服装 + 徽章系统
 * - 支持多种表情状态和尺寸变体
 */
class MascotLionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 当前装备
    var outfit: Outfit = Outfit()
        set(value) {
            field = value
            invalidate()
        }

    // 表情状态
    var currentEmotion: MascotEmotion = MascotEmotion.NORMAL
        private set

    // 尺寸模式
    var mascotSize: MascotSize = MascotSize.LARGE
        set(value) {
            field = value
            requestLayout()
        }

    // 简化模式（小尺寸时减少细节）
    var simplifiedMode: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    // 动画状态
    private var breatheScale = 1f
    private var isBlinking = false
    private var isHappy = false
    private var jumpOffset = 0f
    private var tailRotation = 0f
    private var armRotation = 0f  // 新增：手臂旋转（挥手动画）

    // 画笔
    private val lionBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.FILL
    }

    private val lionFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FCD34D")
        style = Paint.Style.FILL
    }

    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#374151")
        style = Paint.Style.FILL
    }

    private val nosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B45309")
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#374151")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 32f
        isFakeBoldText = true
    }

    // Handler for animations
    private val handler = Handler(Looper.getMainLooper())

    // 呼吸动画
    private val breatheAnimator = ValueAnimator.ofFloat(1f, 1.02f).apply {
        duration = 3000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animation ->
            breatheScale = animation.animatedValue as Float
            if (!isHappy) invalidate()
        }
    }

    init {
        setOnClickListener {
            triggerHappyAnimation()
        }
        breatheAnimator.start()
        startBlinkAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        breatheAnimator.cancel()
        handler.removeCallbacksAndMessages(null)
    }

    private fun triggerHappyAnimation() {
        isHappy = true
        
        // 跳跃动画
        val jumpAnimator = ValueAnimator.ofFloat(0f, -20f, 0f).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                jumpOffset = animation.animatedValue as Float
                invalidate()
            }
        }
        
        // 尾巴摆动动画
        val waveAnimator = ValueAnimator.ofFloat(0f, -10f, 10f, -10f, 10f, 0f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                tailRotation = animation.animatedValue as Float
                invalidate()
            }
        }
        
        jumpAnimator.start()
        waveAnimator.start()
        
        handler.postDelayed({
            isHappy = false
            tailRotation = 0f
            invalidate()
        }, 1000)
    }

    /**
     * 设置小狮子表情
     */
    fun setEmotion(emotion: MascotEmotion) {
        currentEmotion = emotion
        invalidate()
    }

    /**
     * 庆祝动画 - 跳跃 + 尾巴摆动 + 庆祝表情
     */
    fun celebrateAnimation() {
        currentEmotion = MascotEmotion.CELEBRATING
        
        val jumpAnimator = ValueAnimator.ofFloat(0f, -30f, 0f).apply {
            duration = 800
            interpolator = BounceInterpolator()
            addUpdateListener { animation ->
                jumpOffset = animation.animatedValue as Float
                invalidate()
            }
        }
        
        val waveAnimator = ValueAnimator.ofFloat(0f, -15f, 15f, -15f, 15f, 0f).apply {
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                tailRotation = animation.animatedValue as Float
                invalidate()
            }
        }
        
        AnimatorSet().apply {
            playTogether(jumpAnimator, waveAnimator)
            start()
        }
        
        handler.postDelayed({
            currentEmotion = MascotEmotion.NORMAL
            invalidate()
        }, 1200)
    }

    /**
     * 挥手动画
     */
    fun waveAnimation() {
        currentEmotion = MascotEmotion.WAVING
        
        val waveAnimator = ValueAnimator.ofFloat(0f, -30f, 30f, -30f, 30f, 0f).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                armRotation = animation.animatedValue as Float
                invalidate()
            }
        }
        waveAnimator.start()
        
        handler.postDelayed({
            currentEmotion = MascotEmotion.NORMAL
            armRotation = 0f
            invalidate()
        }, 2000)
    }

    private fun startBlinkAnimation() {
        handler.postDelayed({
            isBlinking = true
            invalidate()
            handler.postDelayed({
                isBlinking = false
                invalidate()
                startBlinkAnimation()
            }, 200)
        }, 4000)
    }

    private fun drawTail(canvas: Canvas, scale: Float) {
        canvas.save()
        canvas.rotate(tailRotation, 160f * scale, 140f * scale)
        
        val tailPath = Path().apply {
            moveTo(160f * scale, 140f * scale)
            quadTo(180f * scale, 120f * scale, 170f * scale, 100f * scale)
            quadTo(160f * scale, 80f * scale, 170f * scale, 80f * scale)
        }
        canvas.drawPath(tailPath, tailPaint)
        
        // 尾巴尖
        canvas.drawCircle(170f * scale, 80f * scale, 10f * scale, nosePaint)
        
        canvas.restore()
    }

    private fun drawBody(canvas: Canvas, scale: Float) {
        // 身体矩形
        val bodyRect = RectF(
            60f * scale, 100f * scale,
            140f * scale, 170f * scale
        )
        canvas.drawRoundRect(bodyRect, 20f * scale, 20f * scale, lionBodyPaint)
        
        // 腹部渐变
        val bellyPath = Path().apply {
            moveTo(60f * scale, 100f * scale)
            quadTo(100f * scale, 120f * scale, 140f * scale, 100f * scale)
        }
        val bellyPaint = Paint(lionFacePaint).apply { alpha = 153 }
        canvas.drawPath(bellyPath, bellyPaint)
    }

    private fun drawLegs(canvas: Canvas, scale: Float) {
        // 左腿
        val leftLeg = Path().apply {
            moveTo(70f * scale, 160f * scale)
            lineTo(70f * scale, 180f * scale)
            arcTo(
                RectF(70f * scale, 175f * scale, 80f * scale, 185f * scale),
                180f, 180f, false
            )
            lineTo(80f * scale, 160f * scale)
            close()
        }
        canvas.drawPath(leftLeg, lionBodyPaint)
        
        // 右腿
        val rightLeg = Path().apply {
            moveTo(120f * scale, 160f * scale)
            lineTo(120f * scale, 180f * scale)
            arcTo(
                RectF(120f * scale, 175f * scale, 130f * scale, 185f * scale),
                180f, 180f, false
            )
            lineTo(130f * scale, 160f * scale)
            close()
        }
        canvas.drawPath(rightLeg, lionBodyPaint)
    }

    private fun drawHead(canvas: Canvas, scale: Float) {
        // 主头部圆
        canvas.drawCircle(100f * scale, 80f * scale, 45f * scale, lionBodyPaint)
        
        // 内脸部圆
        canvas.drawCircle(100f * scale, 80f * scale, 35f * scale, lionFacePaint)
        
        // 耳朵
        canvas.drawCircle(65f * scale, 55f * scale, 12f * scale, lionBodyPaint)
        canvas.drawCircle(65f * scale, 55f * scale, 8f * scale, lionFacePaint)
        canvas.drawCircle(135f * scale, 55f * scale, 12f * scale, lionBodyPaint)
        canvas.drawCircle(135f * scale, 55f * scale, 8f * scale, lionFacePaint)
    }

    private fun drawFace(canvas: Canvas, scale: Float) {
        // 根据表情绘制不同的脸部
        when (currentEmotion) {
            MascotEmotion.SAD -> drawSadFace(canvas, scale)
            MascotEmotion.THINKING -> drawThinkingFace(canvas, scale)
            MascotEmotion.SLEEPING -> drawSleepingFace(canvas, scale)
            MascotEmotion.CONFUSED -> drawConfusedFace(canvas, scale)
            MascotEmotion.CELEBRATING -> drawCelebratingFace(canvas, scale)
            else -> drawNormalFace(canvas, scale)
        }
    }

    private fun drawNormalFace(canvas: Canvas, scale: Float) {
        canvas.save()
        
        // 眼睛 (眨眼时压扁)
        if (isBlinking) {
            canvas.scale(1f, 0.1f, 100f * scale, 75f * scale)
        }
        canvas.drawCircle(85f * scale, 75f * scale, 5f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 75f * scale, 5f * scale, eyePaint)
        
        canvas.restore()
        
        // 嘴巴 (开心时弧度更大)
        val mouthPath = Path()
        if (isHappy || currentEmotion == MascotEmotion.HAPPY) {
            mouthPath.moveTo(90f * scale, 90f * scale)
            mouthPath.quadTo(100f * scale, 100f * scale, 110f * scale, 90f * scale)
        } else {
            mouthPath.moveTo(95f * scale, 90f * scale)
            mouthPath.quadTo(100f * scale, 95f * scale, 105f * scale, 90f * scale)
        }
        canvas.drawPath(mouthPath, strokePaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
    }

    private fun drawSadFace(canvas: Canvas, scale: Float) {
        // 眼睛
        canvas.drawCircle(85f * scale, 75f * scale, 5f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 75f * scale, 5f * scale, eyePaint)
        
        // 伤心的嘴巴（向下弯曲）
        val mouthPath = Path().apply {
            moveTo(90f * scale, 95f * scale)
            quadTo(100f * scale, 85f * scale, 110f * scale, 95f * scale)
        }
        canvas.drawPath(mouthPath, strokePaint)
        
        // 眼泪
        val tearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#60A5FA")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(88f * scale, 82f * scale, 2f * scale, tearPaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
    }

    private fun drawThinkingFace(canvas: Canvas, scale: Float) {
        // 眼睛向上看
        canvas.drawCircle(85f * scale, 73f * scale, 5f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 73f * scale, 5f * scale, eyePaint)
        
        // 思考的嘴巴（小圆形）
        canvas.drawCircle(100f * scale, 92f * scale, 3f * scale, strokePaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
        
        // 思考泡泡
        if (!simplifiedMode) {
            val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(130f * scale, 50f * scale, 6f * scale, bubblePaint)
            canvas.drawCircle(125f * scale, 58f * scale, 4f * scale, bubblePaint)
            canvas.drawCircle(122f * scale, 64f * scale, 2f * scale, bubblePaint)
        }
    }

    private fun drawSleepingFace(canvas: Canvas, scale: Float) {
        // 闭着的眼睛（横线）
        val sleepPaint = Paint(strokePaint).apply {
            strokeWidth = 4f * scale
        }
        canvas.drawLine(80f * scale, 75f * scale, 90f * scale, 75f * scale, sleepPaint)
        canvas.drawLine(110f * scale, 75f * scale, 120f * scale, 75f * scale, sleepPaint)
        
        // 微笑的嘴巴
        val mouthPath = Path().apply {
            moveTo(93f * scale, 90f * scale)
            quadTo(100f * scale, 93f * scale, 107f * scale, 90f * scale)
        }
        canvas.drawPath(mouthPath, strokePaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
        
        // ZZZ 睡眠符号
        if (!simplifiedMode) {
            val zzzPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#9CA3AF")
                textSize = 16f * scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Z", 125f * scale, 55f * scale, zzzPaint)
            canvas.drawText("Z", 132f * scale, 45f * scale, zzzPaint)
        }
    }

    private fun drawConfusedFace(canvas: Canvas, scale: Float) {
        // 一个眼睛大一个眼睛小
        canvas.drawCircle(85f * scale, 75f * scale, 6f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 75f * scale, 4f * scale, eyePaint)
        
        // 波浪形嘴巴
        val mouthPath = Path().apply {
            moveTo(90f * scale, 90f * scale)
            quadTo(95f * scale, 93f * scale, 100f * scale, 90f * scale)
            quadTo(105f * scale, 87f * scale, 110f * scale, 90f * scale)
        }
        canvas.drawPath(mouthPath, strokePaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
        
        // 问号
        if (!simplifiedMode) {
            val questionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F59E0B")
                textSize = 20f * scale
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("?", 130f * scale, 60f * scale, questionPaint)
        }
    }

    private fun drawCelebratingFace(canvas: Canvas, scale: Float) {
        // 星星眼睛
        canvas.drawCircle(85f * scale, 75f * scale, 6f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 75f * scale, 6f * scale, eyePaint)
        
        // 超大笑容
        val mouthPath = Path().apply {
            moveTo(85f * scale, 90f * scale)
            quadTo(100f * scale, 105f * scale, 115f * scale, 90f * scale)
        }
        canvas.drawPath(mouthPath, strokePaint)
        
        // 鼻子
        canvas.drawCircle(100f * scale, 85f * scale, 4f * scale, nosePaint)
        
        // 火花效果
        if (!simplifiedMode) {
            val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FBBF24")
                style = Paint.Style.FILL
            }
            listOf(
                Pair(70f, 60f),
                Pair(130f, 60f),
                Pair(75f, 45f),
                Pair(125f, 45f)
            ).forEach { (x, y) ->
                canvas.drawCircle(x * scale, y * scale, 2f * scale, sparklePaint)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val scale = min(w / 200f, h / 200f)
        
        canvas.save()
        canvas.translate(w / 2, h / 2)
        canvas.translate(-100f * scale, -100f * scale + jumpOffset)
        
        if (!isHappy) {
            canvas.scale(breatheScale, breatheScale, 100f * scale, 100f * scale)
        }

        // 绘制顺序: 尾巴 → 身体 → 腿 → 头部 → 脸部 → 身体装备 → 徽章 → 头部装备 → 脸部装备
        drawTail(canvas, scale)
        drawBody(canvas, scale)
        drawLegs(canvas, scale)
        drawHead(canvas, scale)
        drawFace(canvas, scale)
        
        // 装备渲染
        drawBodyOutfit(canvas, scale)
        drawBadge(canvas, scale)
        drawHeadOutfit(canvas, scale)
        drawFaceOutfit(canvas, scale)

        canvas.restore()
    }

    // ==================== 身体装备渲染 ====================
    
    private fun drawBodyOutfit(canvas: Canvas, scale: Float) {
        when (outfit.body) {
            "body_white_shirt" -> drawWhiteShirt(canvas, scale)
            "shirt_nus" -> drawNUSTee(canvas, scale)
            "shirt_hoodie" -> drawHoodie(canvas, scale)
            "body_plaid" -> drawPlaidShirt(canvas, scale)
            "body_suit" -> drawSuit(canvas, scale)
            "body_coat" -> drawLabCoat(canvas, scale)
            "body_sports" -> drawSportsJersey(canvas, scale)
            "body_kimono" -> drawKimono(canvas, scale)
            "body_tux" -> drawTuxedo(canvas, scale)
            "body_superhero" -> drawSuperheroCape(canvas, scale)
            "body_doctor" -> drawDoctorCoat(canvas, scale)
            "body_pilot" -> drawPilotUniform(canvas, scale)
            "body_ninja" -> drawNinjaOutfit(canvas, scale)
            "body_scrubs" -> drawMedicalScrubs(canvas, scale)
            "body_polo" -> drawNursePolo(canvas, scale)
        }
    }

    private fun drawWhiteShirt(canvas: Canvas, scale: Float) {
        val shirtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        // 衬衫主体
        val shirtRect = RectF(
            60f * scale, 103f * scale,
            140f * scale, 158f * scale
        )
        canvas.drawRoundRect(shirtRect, 8f * scale, 8f * scale, shirtPaint)
        
        // 领子
        val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val collarPath = Path().apply {
            moveTo(90f * scale, 103f * scale)
            lineTo(85f * scale, 110f * scale)
            lineTo(95f * scale, 115f * scale)
            close()
        }
        canvas.drawPath(collarPath, collarPaint)
        
        val collarPath2 = Path().apply {
            moveTo(110f * scale, 103f * scale)
            lineTo(115f * scale, 110f * scale)
            lineTo(105f * scale, 115f * scale)
            close()
        }
        canvas.drawPath(collarPath2, collarPaint)
        
        // 纽扣线
        val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5E7EB")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        canvas.drawLine(100f * scale, 110f * scale, 100f * scale, 148f * scale, buttonPaint)
        
        // 纽扣
        val buttonCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(100f * scale, 118f * scale, 2f * scale, buttonCirclePaint)
        canvas.drawCircle(100f * scale, 128f * scale, 2f * scale, buttonCirclePaint)
        canvas.drawCircle(100f * scale, 138f * scale, 2f * scale, buttonCirclePaint)
    }

    private fun drawNUSTee(canvas: Canvas, scale: Float) {
        val teePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        val teeRect = RectF(
            62f * scale, 105f * scale,
            138f * scale, 155f * scale
        )
        canvas.drawRoundRect(teeRect, 10f * scale, 10f * scale, teePaint)
        
        // "NUS" 文字
        val nusPaint = Paint(textPaint).apply {
            color = Color.parseColor("#F97316")
            textSize = 32f * scale
        }
        canvas.drawText("NUS", 100f * scale, 140f * scale, nusPaint)
    }

    private fun drawHoodie(canvas: Canvas, scale: Float) {
        val hoodiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B82F6")
            style = Paint.Style.FILL
        }
        
        val hoodieRect = RectF(
            58f * scale, 102f * scale,
            142f * scale, 162f * scale
        )
        canvas.drawRoundRect(hoodieRect, 15f * scale, 15f * scale, hoodiePaint)
        
        // 拉链线
        val zipperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            alpha = 25
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(80f * scale, 102f * scale, 80f * scale, 140f * scale, zipperPaint)
    }

    private fun drawPlaidShirt(canvas: Canvas, scale: Float) {
        val plaidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            style = Paint.Style.FILL
        }
        
        val plaidRect = RectF(
            60f * scale, 100f * scale,
            140f * scale, 170f * scale
        )
        canvas.drawRoundRect(plaidRect, 20f * scale, 20f * scale, plaidPaint)
        
        // 格子线
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 51
            style = Paint.Style.STROKE
            strokeWidth = 8f * scale
        }
        
        // 竖线
        listOf(70f, 90f, 110f, 130f).forEach { x ->
            canvas.drawLine(x * scale, 100f * scale, x * scale, 170f * scale, linePaint)
        }
        
        // 横线
        listOf(120f, 140f).forEach { y ->
            canvas.drawLine(60f * scale, y * scale, 140f * scale, y * scale, linePaint)
        }
    }

    private fun drawSuit(canvas: Canvas, scale: Float) {
        // 黑色西装
        val suitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        
        val suitPath = Path().apply {
            moveTo(60f * scale, 100f * scale)
            lineTo(140f * scale, 100f * scale)
            lineTo(140f * scale, 170f * scale)
            lineTo(60f * scale, 170f * scale)
            close()
        }
        canvas.drawPath(suitPath, suitPaint)
        
        // 红色领带
        val tiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        
        val tiePath = Path().apply {
            moveTo(100f * scale, 100f * scale)
            lineTo(90f * scale, 130f * scale)
            lineTo(100f * scale, 160f * scale)
            lineTo(110f * scale, 130f * scale)
            close()
        }
        canvas.drawPath(tiePath, tiePaint)
        
        // 白色翻领
        val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 25
            style = Paint.Style.FILL
        }
        
        val collarPath = Path().apply {
            moveTo(60f * scale, 100f * scale)
            lineTo(90f * scale, 130f * scale)
            lineTo(60f * scale, 150f * scale)
            close()
        }
        canvas.drawPath(collarPath, collarPaint)
        
        val collarPath2 = Path().apply {
            moveTo(140f * scale, 100f * scale)
            lineTo(110f * scale, 130f * scale)
            lineTo(140f * scale, 150f * scale)
            close()
        }
        canvas.drawPath(collarPath2, collarPaint)
    }

    private fun drawLabCoat(canvas: Canvas, scale: Float) {
        val coatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        val coatStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        
        val coatRect = RectF(
            58f * scale, 100f * scale,
            142f * scale, 175f * scale
        )
        canvas.drawRoundRect(coatRect, 15f * scale, 15f * scale, coatPaint)
        canvas.drawRoundRect(coatRect, 15f * scale, 15f * scale, coatStrokePaint)
        
        // 中线
        val centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(100f * scale, 100f * scale, 100f * scale, 175f * scale, centerLinePaint)
        
        // 领口
        val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(100f * scale, 100f * scale, 80f * scale, 120f * scale, collarPaint)
        canvas.drawLine(100f * scale, 100f * scale, 120f * scale, 120f * scale, collarPaint)
    }

    private fun drawSportsJersey(canvas: Canvas, scale: Float) {
        val jerseyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        val jerseyRect = RectF(60f * scale, 102f * scale, 140f * scale, 162f * scale)
        canvas.drawRoundRect(jerseyRect, 15f * scale, 15f * scale, jerseyPaint)

        // White side stripes
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f * scale
        }
        canvas.drawLine(65f * scale, 102f * scale, 65f * scale, 162f * scale, stripePaint)
        canvas.drawLine(135f * scale, 102f * scale, 135f * scale, 162f * scale, stripePaint)

        // Number "7"
        val numPaint = Paint(textPaint).apply {
            color = Color.WHITE
            textSize = 36f * scale
        }
        canvas.drawText("7", 100f * scale, 145f * scale, numPaint)
    }

    private fun drawKimono(canvas: Canvas, scale: Float) {
        val kimonoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7C3AED")
            style = Paint.Style.FILL
        }
        val kimonoRect = RectF(58f * scale, 100f * scale, 142f * scale, 175f * scale)
        canvas.drawRoundRect(kimonoRect, 12f * scale, 12f * scale, kimonoPaint)

        // Obi sash
        val obiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        val obiRect = RectF(58f * scale, 135f * scale, 142f * scale, 148f * scale)
        canvas.drawRect(obiRect, obiPaint)

        // V-shaped collar
        val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DDD6FE")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(100f * scale, 100f * scale, 80f * scale, 135f * scale, collarPaint)
        canvas.drawLine(100f * scale, 100f * scale, 120f * scale, 135f * scale, collarPaint)
    }

    private fun drawTuxedo(canvas: Canvas, scale: Float) {
        // Black jacket
        val tuxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111827")
            style = Paint.Style.FILL
        }
        val tuxRect = RectF(58f * scale, 100f * scale, 142f * scale, 172f * scale)
        canvas.drawRoundRect(tuxRect, 15f * scale, 15f * scale, tuxPaint)

        // White shirt front
        val shirtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val shirtPath = Path().apply {
            moveTo(90f * scale, 100f * scale)
            lineTo(110f * scale, 100f * scale)
            lineTo(108f * scale, 172f * scale)
            lineTo(92f * scale, 172f * scale)
            close()
        }
        canvas.drawPath(shirtPath, shirtPaint)

        // Bow tie
        val bowTiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(100f * scale, 108f * scale, 5f * scale, bowTiePaint)
        val bowLeft = Path().apply {
            moveTo(95f * scale, 108f * scale)
            lineTo(86f * scale, 104f * scale)
            lineTo(86f * scale, 112f * scale)
            close()
        }
        canvas.drawPath(bowLeft, bowTiePaint)
        val bowRight = Path().apply {
            moveTo(105f * scale, 108f * scale)
            lineTo(114f * scale, 104f * scale)
            lineTo(114f * scale, 112f * scale)
            close()
        }
        canvas.drawPath(bowRight, bowTiePaint)
    }

    private fun drawSuperheroCape(canvas: Canvas, scale: Float) {
        // Cape behind body
        val capePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        val capePath = Path().apply {
            moveTo(55f * scale, 100f * scale)
            lineTo(45f * scale, 180f * scale)
            lineTo(155f * scale, 180f * scale)
            lineTo(145f * scale, 100f * scale)
            close()
        }
        canvas.drawPath(capePath, capePaint)

        // Body suit (blue)
        val suitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2563EB")
            style = Paint.Style.FILL
        }
        val suitRect = RectF(62f * scale, 102f * scale, 138f * scale, 158f * scale)
        canvas.drawRoundRect(suitRect, 12f * scale, 12f * scale, suitPaint)

        // Shield emblem
        val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        val emblemPath = Path().apply {
            moveTo(100f * scale, 118f * scale)
            lineTo(92f * scale, 125f * scale)
            lineTo(95f * scale, 140f * scale)
            lineTo(100f * scale, 145f * scale)
            lineTo(105f * scale, 140f * scale)
            lineTo(108f * scale, 125f * scale)
            close()
        }
        canvas.drawPath(emblemPath, emblemPaint)
    }

    private fun drawDoctorCoat(canvas: Canvas, scale: Float) {
        val coatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val coatStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        val coatRect = RectF(58f * scale, 100f * scale, 142f * scale, 175f * scale)
        canvas.drawRoundRect(coatRect, 15f * scale, 15f * scale, coatPaint)
        canvas.drawRoundRect(coatRect, 15f * scale, 15f * scale, coatStroke)

        // Center line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
        }
        canvas.drawLine(100f * scale, 100f * scale, 100f * scale, 175f * scale, linePaint)

        // Pocket
        val pocketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5E7EB")
            style = Paint.Style.FILL
        }
        val pocketRect = RectF(65f * scale, 140f * scale, 85f * scale, 158f * scale)
        canvas.drawRoundRect(pocketRect, 3f * scale, 3f * scale, pocketPaint)

        // Stethoscope hint (cross)
        val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            style = Paint.Style.FILL
        }
        canvas.drawRect(95f * scale, 118f * scale, 105f * scale, 122f * scale, crossPaint)
        canvas.drawRect(98f * scale, 115f * scale, 102f * scale, 125f * scale, crossPaint)
    }

    private fun drawPilotUniform(canvas: Canvas, scale: Float) {
        // Dark blue uniform
        val uniformPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A5F")
            style = Paint.Style.FILL
        }
        val uniformRect = RectF(60f * scale, 100f * scale, 140f * scale, 168f * scale)
        canvas.drawRoundRect(uniformRect, 15f * scale, 15f * scale, uniformPaint)

        // Gold buttons
        val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(100f * scale, 115f * scale, 3f * scale, buttonPaint)
        canvas.drawCircle(100f * scale, 130f * scale, 3f * scale, buttonPaint)
        canvas.drawCircle(100f * scale, 145f * scale, 3f * scale, buttonPaint)

        // Shoulder epaulettes
        val epaulettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        val leftEpaulette = RectF(60f * scale, 100f * scale, 78f * scale, 106f * scale)
        canvas.drawRoundRect(leftEpaulette, 3f * scale, 3f * scale, epaulettePaint)
        val rightEpaulette = RectF(122f * scale, 100f * scale, 140f * scale, 106f * scale)
        canvas.drawRoundRect(rightEpaulette, 3f * scale, 3f * scale, epaulettePaint)
    }

    private fun drawNinjaOutfit(canvas: Canvas, scale: Float) {
        // Dark ninja suit
        val ninjaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            style = Paint.Style.FILL
        }
        val ninjaRect = RectF(60f * scale, 100f * scale, 140f * scale, 168f * scale)
        canvas.drawRoundRect(ninjaRect, 12f * scale, 12f * scale, ninjaPaint)

        // Belt
        val beltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        val beltRect = RectF(60f * scale, 132f * scale, 140f * scale, 140f * scale)
        canvas.drawRect(beltRect, beltPaint)

        // Belt knot
        canvas.drawCircle(100f * scale, 136f * scale, 5f * scale, beltPaint)

        // Diagonal wrap line
        val wrapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#374151")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(70f * scale, 105f * scale, 130f * scale, 130f * scale, wrapPaint)
    }

    private fun drawMedicalScrubs(canvas: Canvas, scale: Float) {
        // Green scrubs
        val scrubsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#059669")
            style = Paint.Style.FILL
        }
        val scrubsRect = RectF(60f * scale, 102f * scale, 140f * scale, 165f * scale)
        canvas.drawRoundRect(scrubsRect, 15f * scale, 15f * scale, scrubsPaint)

        // V-neck
        val neckPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#047857")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(100f * scale, 102f * scale, 88f * scale, 118f * scale, neckPaint)
        canvas.drawLine(100f * scale, 102f * scale, 112f * scale, 118f * scale, neckPaint)

        // Chest pocket
        val pocketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#047857")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        val pocketRect = RectF(112f * scale, 115f * scale, 128f * scale, 130f * scale)
        canvas.drawRoundRect(pocketRect, 2f * scale, 2f * scale, pocketPaint)
    }

    private fun drawNursePolo(canvas: Canvas, scale: Float) {
        // Light pink polo
        val poloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FDA4AF")
            style = Paint.Style.FILL
        }
        val poloRect = RectF(62f * scale, 102f * scale, 138f * scale, 160f * scale)
        canvas.drawRoundRect(poloRect, 12f * scale, 12f * scale, poloPaint)

        // Collar
        val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FB7185")
            style = Paint.Style.FILL
        }
        val collarLeft = Path().apply {
            moveTo(85f * scale, 102f * scale)
            lineTo(100f * scale, 102f * scale)
            lineTo(90f * scale, 115f * scale)
            close()
        }
        canvas.drawPath(collarLeft, collarPaint)
        val collarRight = Path().apply {
            moveTo(100f * scale, 102f * scale)
            lineTo(115f * scale, 102f * scale)
            lineTo(110f * scale, 115f * scale)
            close()
        }
        canvas.drawPath(collarRight, collarPaint)

        // Cross emblem
        val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E11D48")
            style = Paint.Style.FILL
        }
        canvas.drawRect(96f * scale, 125f * scale, 104f * scale, 128f * scale, crossPaint)
        canvas.drawRect(98f * scale, 123f * scale, 102f * scale, 130f * scale, crossPaint)
    }

    // ==================== 徽章渲染 ====================
    
    private fun drawBadge(canvas: Canvas, scale: Float) {
        if (outfit.badge == "none" || outfit.badge.isEmpty()) return
        
        val badgeX = 115f * scale
        val badgeY = 140f * scale
        val badgeRadius = 14f * scale
        
        // 白色圆形背景
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(4f * scale, 0f, 2f * scale, Color.parseColor("#40000000"))
        }
        canvas.drawCircle(badgeX, badgeY, badgeRadius, badgeBgPaint)
        
        // 边框
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        canvas.drawCircle(badgeX, badgeY, badgeRadius, badgeBorderPaint)
        
        // 徽章图标 (emoji)
        val badgeIcon = getBadgeIcon(outfit.badge)
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 24f * scale
        }
        val textBounds = Rect()
        badgeTextPaint.getTextBounds(badgeIcon, 0, badgeIcon.length, textBounds)
        canvas.drawText(badgeIcon, badgeX, badgeY + textBounds.height() / 2, badgeTextPaint)
    }

    private fun getBadgeIcon(badgeId: String): String {
        return when (badgeId) {
            "a1", "1" -> "🌱"
            "a2", "2" -> "🚌"
            "a3", "3" -> "🥾"
            "a4", "4" -> "♻️"
            "a5", "5" -> "🌅"
            "a6", "6" -> "🦉"
            else -> ""
        }
    }

    // ==================== 头部装备渲染 ====================
    
    private fun drawHeadOutfit(canvas: Canvas, scale: Float) {
        when (outfit.head) {
            "hat_grad" -> drawGradCap(canvas, scale)
            "hat_cap" -> drawOrangeCap(canvas, scale)
            "hat_helmet" -> drawSafetyHelmet(canvas, scale)
            "hat_beret" -> drawBeret(canvas, scale)
            "hat_crown" -> drawCrown(canvas, scale)
            "hat_party" -> drawPartyHat(canvas, scale)
            "hat_beanie" -> drawBeanie(canvas, scale)
            "hat_cowboy" -> drawCowboyHat(canvas, scale)
            "hat_chef" -> drawChefHat(canvas, scale)
            "hat_wizard" -> drawWizardHat(canvas, scale)
        }
    }

    private fun drawGradCap(canvas: Canvas, scale: Float) {
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        
        // 帽顶
        val capTop = RectF(60f * scale, 35f * scale, 140f * scale, 45f * scale)
        canvas.drawRect(capTop, capPaint)
        
        // 三角帽身
        val capPath = Path().apply {
            moveTo(70f * scale, 35f * scale)
            lineTo(130f * scale, 35f * scale)
            lineTo(100f * scale, 10f * scale)
            close()
        }
        canvas.drawPath(capPath, capPaint)
        
        // 流苏
        val tasselPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FCD34D")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(130f * scale, 35f * scale, 135f * scale, 60f * scale, tasselPaint)
    }

    private fun drawOrangeCap(canvas: Canvas, scale: Float) {
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F97316")
            style = Paint.Style.FILL
        }
        
        // 帽子主体
        val capPath = Path().apply {
            moveTo(60f * scale, 50f * scale)
            quadTo(100f * scale, 20f * scale, 140f * scale, 50f * scale)
        }
        canvas.drawPath(capPath, capPaint)
        
        // 帽舌
        val visorRect = RectF(130f * scale, 45f * scale, 150f * scale, 50f * scale)
        canvas.drawRoundRect(visorRect, 2f * scale, 2f * scale, capPaint)
    }

    private fun drawSafetyHelmet(canvas: Canvas, scale: Float) {
        val helmetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        
        val helmetStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D97706")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        
        // 帽子主体
        val helmetPath = Path().apply {
            moveTo(55f * scale, 55f * scale)
            quadTo(100f * scale, 20f * scale, 145f * scale, 55f * scale)
        }
        canvas.drawPath(helmetPath, helmetPaint)
        canvas.drawPath(helmetPath, helmetStroke)
        
        // 帽檐
        val brimRect = RectF(55f * scale, 55f * scale, 145f * scale, 65f * scale)
        canvas.drawRoundRect(brimRect, 2f * scale, 2f * scale, helmetPaint)
        canvas.drawRoundRect(brimRect, 2f * scale, 2f * scale, helmetStroke)
    }

    private fun drawBeret(canvas: Canvas, scale: Float) {
        val beretPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        
        // 贝雷帽主体
        val beretPath = Path().apply {
            moveTo(150f * scale, 40f * scale)
            quadTo(120f * scale, 20f * scale, 70f * scale, 45f * scale)
            quadTo(60f * scale, 55f * scale, 130f * scale, 55f * scale)
            quadTo(160f * scale, 55f * scale, 150f * scale, 40f * scale)
        }
        canvas.drawPath(beretPath, beretPaint)
        
        // 顶部小球
        val pompomRect = RectF(98f * scale, 20f * scale, 102f * scale, 28f * scale)
        canvas.drawRect(pompomRect, beretPaint)
    }

    private fun drawCrown(canvas: Canvas, scale: Float) {
        val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        // Crown base
        val baseRect = RectF(65f * scale, 40f * scale, 135f * scale, 55f * scale)
        canvas.drawRect(baseRect, crownPaint)
        // Crown points
        val crownPath = Path().apply {
            moveTo(65f * scale, 40f * scale)
            lineTo(75f * scale, 20f * scale)
            lineTo(85f * scale, 35f * scale)
            lineTo(100f * scale, 15f * scale)
            lineTo(115f * scale, 35f * scale)
            lineTo(125f * scale, 20f * scale)
            lineTo(135f * scale, 40f * scale)
            close()
        }
        canvas.drawPath(crownPath, crownPaint)
        // Gems
        val gemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(85f * scale, 46f * scale, 3f * scale, gemPaint)
        canvas.drawCircle(100f * scale, 46f * scale, 3f * scale, gemPaint)
        canvas.drawCircle(115f * scale, 46f * scale, 3f * scale, gemPaint)
    }

    private fun drawPartyHat(canvas: Canvas, scale: Float) {
        val hatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EC4899")
            style = Paint.Style.FILL
        }
        // Cone shape
        val conePath = Path().apply {
            moveTo(70f * scale, 55f * scale)
            lineTo(100f * scale, 10f * scale)
            lineTo(130f * scale, 55f * scale)
            close()
        }
        canvas.drawPath(conePath, hatPaint)
        // Stripe decoration
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(80f * scale, 45f * scale, 120f * scale, 45f * scale, stripePaint)
        canvas.drawLine(88f * scale, 32f * scale, 112f * scale, 32f * scale, stripePaint)
        // Pompom on top
        val pompomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(100f * scale, 10f * scale, 5f * scale, pompomPaint)
    }

    private fun drawBeanie(canvas: Canvas, scale: Float) {
        val beaniePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6366F1")
            style = Paint.Style.FILL
        }
        // Beanie dome
        val beaniePath = Path().apply {
            moveTo(58f * scale, 55f * scale)
            quadTo(100f * scale, 15f * scale, 142f * scale, 55f * scale)
        }
        canvas.drawPath(beaniePath, beaniePaint)
        // Fold/rim
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F46E5")
            style = Paint.Style.FILL
        }
        val rimRect = RectF(58f * scale, 48f * scale, 142f * scale, 58f * scale)
        canvas.drawRoundRect(rimRect, 3f * scale, 3f * scale, rimPaint)
        // Pompom on top
        val pompomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#818CF8")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(100f * scale, 22f * scale, 7f * scale, pompomPaint)
    }

    private fun drawCowboyHat(canvas: Canvas, scale: Float) {
        val hatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#92400E")
            style = Paint.Style.FILL
        }
        // Wide brim
        val brimPath = Path().apply {
            moveTo(40f * scale, 55f * scale)
            quadTo(100f * scale, 48f * scale, 160f * scale, 55f * scale)
            lineTo(155f * scale, 60f * scale)
            quadTo(100f * scale, 52f * scale, 45f * scale, 60f * scale)
            close()
        }
        canvas.drawPath(brimPath, hatPaint)
        // Crown of the hat
        val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#78350F")
            style = Paint.Style.FILL
        }
        val crownPath = Path().apply {
            moveTo(70f * scale, 55f * scale)
            quadTo(75f * scale, 25f * scale, 100f * scale, 22f * scale)
            quadTo(125f * scale, 25f * scale, 130f * scale, 55f * scale)
            close()
        }
        canvas.drawPath(crownPath, crownPaint)
        // Hat band
        val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(72f * scale, 48f * scale, 128f * scale, 48f * scale, bandPaint)
    }

    private fun drawChefHat(canvas: Canvas, scale: Float) {
        val hatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val hatStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        // Tall part
        val tallRect = RectF(70f * scale, 10f * scale, 130f * scale, 50f * scale)
        canvas.drawRoundRect(tallRect, 15f * scale, 15f * scale, hatPaint)
        canvas.drawRoundRect(tallRect, 15f * scale, 15f * scale, hatStroke)
        // Puffy top
        canvas.drawCircle(80f * scale, 15f * scale, 12f * scale, hatPaint)
        canvas.drawCircle(100f * scale, 10f * scale, 14f * scale, hatPaint)
        canvas.drawCircle(120f * scale, 15f * scale, 12f * scale, hatPaint)
        // Band at bottom
        val bandRect = RectF(68f * scale, 46f * scale, 132f * scale, 56f * scale)
        canvas.drawRoundRect(bandRect, 3f * scale, 3f * scale, hatPaint)
        canvas.drawRoundRect(bandRect, 3f * scale, 3f * scale, hatStroke)
    }

    private fun drawWizardHat(canvas: Canvas, scale: Float) {
        val hatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4338CA")
            style = Paint.Style.FILL
        }
        // Cone shape (tall, slightly bent)
        val conePath = Path().apply {
            moveTo(55f * scale, 55f * scale)
            lineTo(110f * scale, -10f * scale)
            lineTo(145f * scale, 55f * scale)
            close()
        }
        canvas.drawPath(conePath, hatPaint)
        // Brim
        val brimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3730A3")
            style = Paint.Style.FILL
        }
        val brimPath = Path().apply {
            moveTo(48f * scale, 52f * scale)
            quadTo(100f * scale, 62f * scale, 152f * scale, 52f * scale)
            quadTo(100f * scale, 58f * scale, 48f * scale, 52f * scale)
        }
        canvas.drawPath(brimPath, brimPaint)
        // Stars decoration
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(90f * scale, 25f * scale, 3f * scale, starPaint)
        canvas.drawCircle(110f * scale, 38f * scale, 2f * scale, starPaint)
        canvas.drawCircle(80f * scale, 42f * scale, 2f * scale, starPaint)
    }

    // ==================== 脸部装备渲染 ====================
    
    private fun drawFaceOutfit(canvas: Canvas, scale: Float) {
        when (outfit.face) {
            "face_glasses_square" -> drawSquareGlasses(canvas, scale)
            "glasses_sun" -> drawSunglasses(canvas, scale)
            "face_goggles" -> drawSafetyGoggles(canvas, scale)
            "glasses_nerd" -> drawNerdGlasses(canvas, scale)
            "glasses_3d" -> draw3DGlasses(canvas, scale)
            "face_mask" -> drawSuperheroMask(canvas, scale)
            "face_monocle" -> drawMonocle(canvas, scale)
            "face_scarf" -> drawScarf(canvas, scale)
            "face_vr" -> drawVRHeadset(canvas, scale)
        }
    }

    private fun drawSquareGlasses(canvas: Canvas, scale: Float) {
        val glassesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
        }
        
        // 左镜框（方形）
        val leftFrame = RectF(75f * scale, 68f * scale, 95f * scale, 82f * scale)
        canvas.drawRect(leftFrame, glassesPaint)
        
        // 右镜框（方形）
        val rightFrame = RectF(105f * scale, 68f * scale, 125f * scale, 82f * scale)
        canvas.drawRect(rightFrame, glassesPaint)
        
        // 鼻梁
        canvas.drawLine(95f * scale, 75f * scale, 105f * scale, 75f * scale, glassesPaint)
        
        // 镜腿
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        canvas.drawLine(75f * scale, 75f * scale, 65f * scale, 75f * scale, framePaint)
        canvas.drawLine(125f * scale, 75f * scale, 135f * scale, 75f * scale, framePaint)
    }

    private fun drawSunglasses(canvas: Canvas, scale: Float) {
        val glassesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        
        // 左镜片
        val leftLens = RectF(75f * scale, 70f * scale, 95f * scale, 80f * scale)
        canvas.drawRoundRect(leftLens, 2f * scale, 2f * scale, glassesPaint)
        
        // 右镜片
        val rightLens = RectF(105f * scale, 70f * scale, 125f * scale, 80f * scale)
        canvas.drawRoundRect(rightLens, 2f * scale, 2f * scale, glassesPaint)
        
        // 鼻梁
        val bridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        canvas.drawLine(95f * scale, 75f * scale, 105f * scale, 75f * scale, bridgePaint)
    }

    private fun drawSafetyGoggles(canvas: Canvas, scale: Float) {
        val goggleLensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#93C5FD")
            alpha = 128
            style = Paint.Style.FILL
        }
        
        val goggleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B82F6")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        
        // 左镜片
        val leftGoggles = RectF(70f * scale, 65f * scale, 95f * scale, 80f * scale)
        canvas.drawRoundRect(leftGoggles, 5f * scale, 5f * scale, goggleLensPaint)
        canvas.drawRoundRect(leftGoggles, 5f * scale, 5f * scale, goggleStrokePaint)
        
        // 右镜片
        val rightGoggles = RectF(105f * scale, 65f * scale, 130f * scale, 80f * scale)
        canvas.drawRoundRect(rightGoggles, 5f * scale, 5f * scale, goggleLensPaint)
        canvas.drawRoundRect(rightGoggles, 5f * scale, 5f * scale, goggleStrokePaint)
        
        // 连接鼻梁
        canvas.drawLine(95f * scale, 72f * scale, 105f * scale, 72f * scale, goggleStrokePaint)
        
        // 侧边带子
        val strapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 6f * scale
        }
        canvas.drawLine(70f * scale, 72f * scale, 55f * scale, 65f * scale, strapPaint)
        canvas.drawLine(130f * scale, 72f * scale, 145f * scale, 65f * scale, strapPaint)
    }

    private fun drawNerdGlasses(canvas: Canvas, scale: Float) {
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            style = Paint.Style.STROKE
            strokeWidth = 4f * scale
        }
        val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 80
            style = Paint.Style.FILL
        }
        // Left lens (round)
        canvas.drawCircle(85f * scale, 75f * scale, 10f * scale, lensPaint)
        canvas.drawCircle(85f * scale, 75f * scale, 10f * scale, framePaint)
        // Right lens (round)
        canvas.drawCircle(115f * scale, 75f * scale, 10f * scale, lensPaint)
        canvas.drawCircle(115f * scale, 75f * scale, 10f * scale, framePaint)
        // Bridge
        canvas.drawLine(95f * scale, 75f * scale, 105f * scale, 75f * scale, framePaint)
        // Tape on bridge
        val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(97f * scale, 73f * scale, 103f * scale, 77f * scale, tapePaint)
    }

    private fun draw3DGlasses(canvas: Canvas, scale: Float) {
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        // Frame
        val frameRect = RectF(70f * scale, 68f * scale, 130f * scale, 82f * scale)
        canvas.drawRoundRect(frameRect, 4f * scale, 4f * scale, framePaint)
        // Left lens (red/cyan)
        val leftLensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            alpha = 180
            style = Paint.Style.FILL
        }
        val leftLens = RectF(73f * scale, 70f * scale, 97f * scale, 80f * scale)
        canvas.drawRoundRect(leftLens, 3f * scale, 3f * scale, leftLensPaint)
        // Right lens (cyan)
        val rightLensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06B6D4")
            alpha = 180
            style = Paint.Style.FILL
        }
        val rightLens = RectF(103f * scale, 70f * scale, 127f * scale, 80f * scale)
        canvas.drawRoundRect(rightLens, 3f * scale, 3f * scale, rightLensPaint)
    }

    private fun drawSuperheroMask(canvas: Canvas, scale: Float) {
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        // Mask shape
        val maskPath = Path().apply {
            moveTo(65f * scale, 75f * scale)
            quadTo(75f * scale, 62f * scale, 100f * scale, 68f * scale)
            quadTo(125f * scale, 62f * scale, 135f * scale, 75f * scale)
            quadTo(125f * scale, 82f * scale, 100f * scale, 80f * scale)
            quadTo(75f * scale, 82f * scale, 65f * scale, 75f * scale)
        }
        canvas.drawPath(maskPath, maskPaint)
        // Eye holes
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(78f * scale, 71f * scale, 92f * scale, 79f * scale), holePaint)
        canvas.drawOval(RectF(108f * scale, 71f * scale, 122f * scale, 79f * scale), holePaint)
        // Eyes inside holes
        canvas.drawCircle(85f * scale, 75f * scale, 4f * scale, eyePaint)
        canvas.drawCircle(115f * scale, 75f * scale, 4f * scale, eyePaint)
    }

    private fun drawMonocle(canvas: Canvas, scale: Float) {
        val monocleFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
        }
        val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 60
            style = Paint.Style.FILL
        }
        // Monocle on right eye
        canvas.drawCircle(115f * scale, 75f * scale, 10f * scale, lensPaint)
        canvas.drawCircle(115f * scale, 75f * scale, 10f * scale, monocleFramePaint)
        // Chain
        val chainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4A017")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }
        val chainPath = Path().apply {
            moveTo(115f * scale, 85f * scale)
            quadTo(120f * scale, 95f * scale, 125f * scale, 110f * scale)
        }
        canvas.drawPath(chainPath, chainPaint)
    }

    private fun drawScarf(canvas: Canvas, scale: Float) {
        val scarfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DC2626")
            style = Paint.Style.FILL
        }
        // Scarf around neck
        val scarfPath = Path().apply {
            moveTo(60f * scale, 90f * scale)
            quadTo(100f * scale, 105f * scale, 140f * scale, 90f * scale)
            lineTo(140f * scale, 100f * scale)
            quadTo(100f * scale, 115f * scale, 60f * scale, 100f * scale)
            close()
        }
        canvas.drawPath(scarfPath, scarfPaint)
        // Hanging end
        val endPath = Path().apply {
            moveTo(110f * scale, 100f * scale)
            lineTo(118f * scale, 130f * scale)
            lineTo(108f * scale, 128f * scale)
            lineTo(100f * scale, 100f * scale)
        }
        canvas.drawPath(endPath, scarfPaint)
        // Stripe on scarf
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FCA5A5")
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
        }
        canvas.drawLine(70f * scale, 95f * scale, 130f * scale, 95f * scale, stripePaint)
    }

    private fun drawVRHeadset(canvas: Canvas, scale: Float) {
        val headsetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            style = Paint.Style.FILL
        }
        // Main headset body
        val headsetRect = RectF(65f * scale, 62f * scale, 135f * scale, 82f * scale)
        canvas.drawRoundRect(headsetRect, 8f * scale, 8f * scale, headsetPaint)
        // Visor highlight
        val visorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6366F1")
            alpha = 180
            style = Paint.Style.FILL
        }
        val visorRect = RectF(70f * scale, 66f * scale, 130f * scale, 78f * scale)
        canvas.drawRoundRect(visorRect, 5f * scale, 5f * scale, visorPaint)
        // Strap
        val strapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#374151")
            style = Paint.Style.STROKE
            strokeWidth = 6f * scale
        }
        canvas.drawLine(65f * scale, 72f * scale, 50f * scale, 65f * scale, strapPaint)
        canvas.drawLine(135f * scale, 72f * scale, 150f * scale, 65f * scale, strapPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 使用预设尺寸
        val desiredSize = (mascotSize.dp * resources.displayMetrics.density).toInt()
        
        // 小尺寸时自动启用简化模式
        simplifiedMode = mascotSize == MascotSize.SMALL || mascotSize == MascotSize.MEDIUM
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }
}
