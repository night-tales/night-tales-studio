import CryptoJS from 'crypto-js';

const SALT = 'night-tales-studio-ai-salt';

export function encryptKey(key: string, uid: string): string {
  if (!key) return '';
  return CryptoJS.AES.encrypt(key, uid + SALT).toString();
}

export function decryptKey(cipher: string, uid: string): string {
  if (!cipher) return '';
  try {
    const bytes = CryptoJS.AES.decrypt(cipher, uid + SALT);
    return bytes.toString(CryptoJS.enc.Utf8);
  } catch (e) {
    console.error('Failed to decrypt key', e);
    return '';
  }
}
