import httpArticleRepository from './httpArticleRepository'
import mockArticleRepository from './mockArticleRepository'

const dataMode = import.meta.env.VITE_DATA_MODE || 'mock'

const articleRepository = dataMode === 'api'
  ? httpArticleRepository
  : mockArticleRepository

export { dataMode }
export default articleRepository
