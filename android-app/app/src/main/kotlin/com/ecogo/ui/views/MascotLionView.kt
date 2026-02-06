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
            "shirt_nus" -> drawNUSTee(canvas, scale)
            "shirt_hoodie" -> drawHoodie(canvas, scale)
            "body_plaid" -> drawPlaidShirt(canvas, scale)
            "body_suit" -> drawSuit(canvas, scale)
            "body_coat" -> drawLabCoat(canvas, scale)
        }
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

    // ==================== 脸部装备渲染 ====================
    
    private fun drawFaceOutfit(canvas: Canvas, scale: Float) {
        when (outfit.face) {
            "glasses_sun" -> drawSunglasses(canvas, scale)
            "face_goggles" -> drawSafetyGoggles(canvas, scale)
        }
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
