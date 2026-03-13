import JSEncrypt from 'jsencrypt'

// RSA公钥（需要从后端获取或配置）
const publicKey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1QQRl0HlrVv6kGqhgonD6TrTVzhWnA9Ysqs0j3GGuHiYJdwvSSEHj25uB8I00Sh6soSAr9Wp5mSN2AhAPCUk48IdP+miejh1N4bd19Cyz5HgVFn9lbvjChghBl+F2D2dkES+O7c6IPbOPnQ7E5MXuZBUlxWAicB3Wnp59gQIDAQAB'

// RSA私钥（仅用于前端解密，实际应该只存储公钥）
const privateKey = 'MIICXQIBAAKBgQC1QQRl0HlrVv6kGqhgonD6TrTVzhWnA9Ysqs0j3GGuHiYJdwvSSEHj25uB8I00Sh6soSAr9Wp5mSN2AhAPCUk48IdP+miejh1N4bd19Cyz5HgVFn9lbvjChghBl+F2D2dkES+O7c6IPbOPnQ7E5MXuZBUlxWAicB3Wnp59gQIDAQABAoGBAKYDKP4AFlXkVlMCN5PmU8mZ2qk0vj2i3SUjqUXNsRxWUW77+/tkWb8Yjv6Z5bZQAcP/nM5DB3kZ4OqvjulY6DC8Erjj3lja5Q8pkEj3QQp6V3qjQEKFE3CfqFwMHANhdER0ENn9W5UaNMlFfjqvc2GKmlOqCLGYRc8I0+uAoECQQDm3F1rP6chkxX5fY3ryRafbuqY2wEDlqps5YZ75/9rZ3IHDQVqcVlC2qEzxubOrB4N6LannM8BKt5rynTYi1lmAkEAx31pxvQFBy+JTXJeErwec9DSMZSbnG2Dg8a3a3GpES7gkbN2n4ISFCzP+uPHYk5ZDhsOItFuW07Nw4zotdXx2QJBAOGaPQSlXg4pFEj81Y4mMX75n+ioYqM3c3xwXaF4sYZFhZtH0CS2VQRUIFcdlzLVRO2aL8kF2mJd2bsZV0MCXECQDTuP2huNv9gwVEVf3YT8uKbScjn08MDFtZY2sKj0TtdZsZQJ8M6oHiW+c5zTmu26BLA8hlnsf7ah9DBKtEklEQJBAKZqAN4jLkv2S0xY6u1l+ztNDKclhHUy8iY13CwkfEF+SYj8mYvjylMD6x0Nb3K2ydMKIT+4DajlbUsCbOmnM+4='

/**
 * 加密字符串
 * @param {string} data 需要加密的数据
 * @returns {string} 加密后的字符串
 */
export function encrypt(data) {
  if (!data) {
    return ''
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(data) || ''
}

/**
 * 解密字符串
 * @param {string} data 需要解密的数据
 * @returns {string} 解密后的字符串
 */
export function decrypt(data) {
  if (!data) {
    return ''
  }
  const decryptor = new JSEncrypt()
  decryptor.setPrivateKey(privateKey)
  return decryptor.decrypt(data) || ''
}
