/**
 * utils/compressImage.js - 前端图片压缩工具
 * 在浏览器端使用 Canvas 对图片进行等比缩放压缩，保留原文件格式
 * @param {File} file - 原始图片文件
 * @param {number} maxWidth - 最大宽度（默认 1200）
 * @param {number} quality - 质量（0-1，默认 0.85）
 * @returns {Promise<File>} 压缩后的 File 对象
 */
export function compressImage(file, maxWidth = 1200, quality = 0.85) {
  return new Promise((resolve, reject) => {
    // GIF 或非图片不压缩
    if (!file.type.startsWith('image/') || file.type === 'image/gif') {
      return resolve(file)
    }
    // 小于 100KB 不压缩
    if (file.size < 100 * 1024) {
      return resolve(file)
    }

    const img = new Image()
    const url = URL.createObjectURL(file)

    img.onload = () => {
      URL.revokeObjectURL(url)
      // 宽度小于 maxWidth 不压缩
      if (img.width <= maxWidth) {
        return resolve(file)
      }

      // 等比缩放计算目标尺寸
      const ratio = maxWidth / img.width
      const canvas = document.createElement('canvas')
      canvas.width = maxWidth
      canvas.height = Math.round(img.height * ratio)

      const ctx = canvas.getContext('2d')
      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)

      // 保留原格式（PNG 保留透明通道，JPEG 压缩，WEBP 压缩）
      const mimeMap = { 'image/png': 'image/png', 'image/jpeg': 'image/jpeg', 'image/webp': 'image/webp' }
      const targetMime = mimeMap[file.type] || 'image/jpeg'
      // 使用原扩展名（PNG→png, JPEG→jpg, WEBP→webp）
      const extMap = { 'image/png': '.png', 'image/jpeg': '.jpg', 'image/webp': '.webp' }
      const newExt = extMap[file.type] || '.jpg'

      canvas.toBlob(
        (blob) => {
          if (!blob) return reject(new Error('图片压缩失败'))
          const newName = file.name.replace(/\.[^.]+$/i, '') + newExt
          const compressed = new File([blob], newName, { type: targetMime, lastModified: Date.now() })
          resolve(compressed)
        },
        targetMime,
        // PNG 无损不失真，直接用质量 1；其他格式用指定 quality
        targetMime === 'image/png' ? 1 : quality
      )
    }

    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片加载失败'))
    }

    img.src = url
  })
}
