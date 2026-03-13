/**
 * 用户权限相关工具函数类型声明
 */

/**
 * 获取token
 */
export function getToken(): string | null;

/**
 * 设置token
 * @param token 
 */
export function setToken(token: string): void;

/**
 * 移除token
 */
export function removeToken(): void;

/**
 * 判断是否已登录
 */
export function isLoggedIn(): boolean;

/**
 * 获取用户信息
 */
export function getUserInfo(): any | null;

/**
 * 设置用户信息
 * @param userInfo 
 */
export function setUserInfo(userInfo: any): void;

/**
 * 移除用户信息
 */
export function removeUserInfo(): void;

/**
 * 解析头像 URL（相对路径补全为当前 origin）
 * @param url 头像路径或 URL
 * @returns 可用的完整头像 URL
 */
export function resolveAvatarUrl(url: string | undefined): string;

/**
 * 退出登录
 */
export function logout(): void;
