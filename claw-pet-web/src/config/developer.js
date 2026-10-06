/**
 * 开发者信息 —— 登录页右上角那张名片
 * ==================================================================
 * 想改名片内容，只改这一个文件就够了。保存后页面自动热更新，不用重启。
 *
 * 字段说明：
 *   name    姓名 / 昵称，显示在名片标题行
 *   desc    一行简介，显示在名字下面
 *   avatar  头像。留空 '' 就用 name 的第一个字做圆形头像（最简单）
 *           想换成自己的照片/图片，见下面「怎么换成自己的头像」
 *   items   联系方式列表，一行一条：
 *             label  左边那列灰字（邮箱 / QQ / 微信 / GitHub …）
 *             value  右边显示的值
 *             href   填了就变成可点击的链接；不填就是纯文字
 *                    邮箱写 mailto:xxx@qq.com，网站直接写 https://...
 *
 * 小设计：value 里带「填」字的那一行会自动隐藏，
 *        所以你可以先只填一部分，没填的不会露出「填写你的QQ」这种占位文字。
 *
 * ------------------------------------------------------------------
 * 怎么换成自己的头像：
 *   1) 把图片丢进 claw-pet-web/src/assets/images/ 目录（比如 me.png）
 *   2) 把下面的 avatar 写成：
 *        avatar: new URL('../assets/images/me.png', import.meta.url).href
 *      （这样打包后路径才会正确。直接写 './me.png' 在打包后会 404）
 *   3) 也可以直接写公网完整地址： avatar: 'https://xxx.com/me.png'
 *
 * 写死路径的写法会让打包后 404 —— 因为 Vite 只处理它「看得见」的静态资源引用。
 */
export const developer = {
  name: 'Jingtao',
  desc: 'Java / Vue 全栈开发',
  avatar: '',

  items: [
    { label: '邮箱', value: '填写你的邮箱', href: '' },
    { label: 'QQ', value: '填写你的QQ' },
    { label: '微信', value: '填写你的微信' },
    { label: 'Gitee', value: 'jia-jingtao1', href: 'https://gitee.com/jia-jingtao1' }

    // 需要更多行就照着加，比如：
    // { label: 'GitHub', value: 'your-id', href: 'https://github.com/your-id' },
    // { label: '博客',   value: 'your-blog.com', href: 'https://your-blog.com' }
  ]
}

export default developer
