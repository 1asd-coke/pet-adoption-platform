<template>
  <div class="aurora-bg" aria-hidden="true">
    <span class="blob b1"></span>
    <span class="blob b2"></span>
    <span class="blob b3"></span>
    <span class="blob b4"></span>
  </div>
</template>

<script setup>
/**
 * AuroraBg - 光晕渐变动态背景（Aurora / Mesh Gradient）
 *
 * 四团柔和光晕在四角缓慢漂移、互相融合，营造氛围感。
 *
 * 性能约定（改之前请先看完）：
 *  - 只用 radial-gradient 造柔光，**不加 filter: blur()**。
 *    blur 会让图层每帧重新栅格化，是这类背景掉帧的头号原因；
 *    而 radial-gradient 从实色渐变到 transparent 本身就是柔和的 alpha 衰减，效果够用。
 *  - 只动 transform + opacity —— 只有这两个走 GPU 合成器，不触发重排重绘。
 *    绝对不要改成 top/left/width/height。
 *  - animation-direction 用 alternate，避免循环衔接处突跳。
 *  - 幅度和周期要够「看得出来」。0.8 亮度 + 6vw 位移实测 3.5s 只走 8px、
 *    灰度又低，看几秒还是像静止的 —— 低对比度下必须靠更大的位移补足运动感。
 *    现在 9vw/-7vh、20~29s，实测 3.5s 走 25px 上下。
 *
 * 主题：颜色只取 var(--brand-primary)，10 套主题自动跟随，不需要额外维护。
 *
 * 🔥 光晕色必须「提亮」后再用，不能直接拿品牌色：
 *   光晕只能比页面底色更深（白底上没法更亮），所以深色品牌色直接当光晕，
 *   渲染出来就是一团灰渍 —— 像脏，不像光。默认主题「简约白」的品牌色是 #2a2a2a，
 *   实测就是这个效果。
 *   这里用 oklch 相对色把亮度统一钳到 0.76、保留原色相与彩度：
 *   深色主题被提亮成灰雾，浅彩主题变成柔和的粉彩，10 套观感一致。
 */
</script>

<style scoped>
.aurora-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  display: block;
  border-radius: 50%;
  /* 兜底：不支持相对色的浏览器直接用品牌色 */
  background: radial-gradient(circle closest-side, var(--brand-primary) 0%, transparent 90%);
  will-change: transform;
  animation: auroraDrift var(--dur, 30s) ease-in-out infinite alternate;
}

/* 注意：这里必须用 @supports 而不是「写两条 background」——
   带 var() 的声明若在计算值阶段失效，整条会变 unset（透明），并不会回退到上一条。 */
@supports (background: oklch(from red 0.76 c h)) {
  .blob {
    background: radial-gradient(
      circle closest-side,
      oklch(from var(--brand-primary) 0.76 c h) 0%,
      transparent 90%
    );
  }
}

.b1 {
  width: clamp(320px, 46vw, 780px);
  height: clamp(320px, 46vw, 780px);
  left: -11%;
  top: -19%;
  opacity: 0.6;
  --dur: 20s;
}

.b2 {
  width: clamp(280px, 38vw, 640px);
  height: clamp(280px, 38vw, 640px);
  right: -12%;
  top: -12%;
  opacity: 0.46;
  --dur: 26s;
  animation-delay: -6s;
}

.b3 {
  width: clamp(300px, 44vw, 740px);
  height: clamp(300px, 44vw, 740px);
  right: -10%;
  bottom: -20%;
  opacity: 0.52;
  --dur: 23s;
  animation-delay: -13s;
}

.b4 {
  width: clamp(240px, 34vw, 560px);
  height: clamp(240px, 34vw, 560px);
  left: -2%;
  bottom: -20%;
  opacity: 0.42;
  --dur: 29s;
  animation-delay: -18s;
}

@keyframes auroraDrift {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }
  to {
    transform: translate3d(9vw, -7vh, 0) scale(1.22);
  }
}

@media (prefers-reduced-motion: reduce) {
  .blob {
    animation: none;
  }
}
</style>
