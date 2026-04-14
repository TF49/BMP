# 验证 Timeout 错误修复脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证 Timeout 错误修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查错误抑制器文件
Write-Host "[1/6] 检查错误抑制器文件..." -ForegroundColor Yellow
if (Test-Path "utils/mp-weixin-error-suppressor.ts") {
    Write-Host "  OK 错误抑制器文件存在" -ForegroundColor Green
} else {
    Write-Host "  ERROR 错误抑制器文件不存在" -ForegroundColor Red
    exit 1
}

# 2. 检查 main.ts 导入顺序
Write-Host "[2/6] 检查 main.ts 导入顺序..." -ForegroundColor Yellow
$mainContent = Get-Content "main.ts" -Raw
if ($mainContent -match "mp-weixin-error-suppressor") {
    Write-Host "  OK 错误抑制器已导入" -ForegroundColor Green
} else {
    Write-Host "  ERROR 错误抑制器未导入" -ForegroundColor Red
    exit 1
}

# 3. 检查 App.vue 错误处理
Write-Host "[3/6] 检查 App.vue 错误处理..." -ForegroundColor Yellow
$appContent = Get-Content "App.vue" -Raw
if ($appContent -match "onError") {
    Write-Host "  OK App.vue 包含错误处理代码" -ForegroundColor Green
} else {
    Write-Host "  WARN App.vue 可能缺少错误处理" -ForegroundColor Yellow
}

# 4. 检查 manifest.json 配置
Write-Host "[4/6] 检查 manifest.json 配置..." -ForegroundColor Yellow
$manifestContent = Get-Content "manifest.json" -Raw
if ($manifestContent -match "compileHotReLoad") {
    Write-Host "  OK manifest.json 包含热重载配置" -ForegroundColor Green
} else {
    Write-Host "  WARN manifest.json 可能缺少配置" -ForegroundColor Yellow
}

# 5. 检查编译输出
Write-Host "[5/6] 检查编译输出..." -ForegroundColor Yellow
if (Test-Path "unpackage/dist/dev/mp-weixin/utils/mp-weixin-error-suppressor.js") {
    Write-Host "  OK 错误抑制器已编译" -ForegroundColor Green
} else {
    Write-Host "  WARN 错误抑制器未编译，请先编译项目" -ForegroundColor Yellow
}

# 6. 检查 app.js 导入
Write-Host "[6/6] 检查 app.js 导入顺序..." -ForegroundColor Yellow
if (Test-Path "unpackage/dist/dev/mp-weixin/app.js") {
    $appJsContent = Get-Content "unpackage/dist/dev/mp-weixin/app.js" -Raw
    if ($appJsContent -match "mp-weixin-error-suppressor") {
        Write-Host "  OK app.js 包含错误抑制器导入" -ForegroundColor Green
    } else {
        Write-Host "  ERROR app.js 缺少错误抑制器导入" -ForegroundColor Red
    }
} else {
    Write-Host "  WARN app.js 未找到，请先编译项目" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 在微信开发者工具中：工具 -> 清除缓存 -> 全部清除" -ForegroundColor White
Write-Host "2. 重新编译项目" -ForegroundColor White
Write-Host "3. 测试登录功能，观察控制台" -ForegroundColor White
Write-Host ""
