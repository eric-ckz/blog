<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <div class="footer-brand-row">
          <span class="brand-mark">文</span>
          <strong>大叔的文字与光影</strong>
        </div>
        <p>以文字记录生活，以光影收藏世界。<br>这里的每一篇，都是认真活过的证据。</p>
      </div>

      <div>
        <p class="footer-heading">栏目</p>
        <ul class="footer-links">
          <li><router-link to="/articles?cat=travel">旅行游记</router-link></li>
          <li><router-link to="/articles?cat=photo">摄影光影</router-link></li>
          <li><router-link to="/articles?cat=literature">文学诗韵</router-link></li>
          <li><router-link to="/articles?cat=city">城市漫步</router-link></li>
        </ul>
      </div>

      <div>
        <p class="footer-heading">站内</p>
        <ul class="footer-links">
          <li><router-link to="/articles">全部文章</router-link></li>
          <li><router-link to="/gallery">光影集</router-link></li>
          <li><router-link to="/archive">时光档案</router-link></li>
          <li><router-link to="/about">关于我</router-link></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <span>© 2020–2026 爱喝咖啡的文艺大叔 · 文章同步自微信公众号</span>
      <span>Stay Hungry, Stay Foolish.</span>
    </div>
  </footer>
</template>
