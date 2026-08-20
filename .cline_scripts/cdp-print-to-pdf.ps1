param(
    [Parameter(Mandatory=$true)][string]$HtmlPath,
    [Parameter(Mandatory=$true)][string]$PdfPath,
    [string]$TabPattern = "",
    [int]$Port = 9222,
    [int]$WaitMs = 1500
)
$ErrorActionPreference = "Stop"

if ($TabPattern -eq "") { $TabPattern = [IO.Path]::GetFileNameWithoutExtension($HtmlPath) }

# 1. Open tab with HTML
$fileUrl = [uri]::EscapeDataString([System.Uri]::new($HtmlPath).AbsoluteUri)
Invoke-RestMethod -Method Put -Uri "http://127.0.0.1:$Port/json/new?$fileUrl" | Out-Null
Start-Sleep -Milliseconds $WaitMs

# 2. Find the tab
$tabs = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/json/list"
$tab = $tabs | Where-Object { $_.type -eq "page" -and $_.url -like "*$TabPattern*" } | Select-Object -First 1
if (-not $tab) { Write-Error "Tab with HTML not found by pattern: $TabPattern" }

# 3. WebSocket -> Page.printToPDF
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$ws.ConnectAsync([System.Uri]::new($tab.webSocketDebuggerUrl), [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$params = @{ printBackground = $true; format = "A4"; marginTop = 0.4; marginBottom = 0.4; marginLeft = 0.4; marginRight = 0.4; preferCSSPageSize = $false }
$cmd = @{ id = 1; method = "Page.printToPDF"; params = $params } | ConvertTo-Json -Compress -Depth 6
$bytes = [System.Text.Encoding]::UTF8.GetBytes($cmd)
$ws.SendAsync([System.ArraySegment[byte]]::new($bytes), [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$buffer = New-Object byte[] 41943040
$mem = New-Object System.IO.MemoryStream
$ct = [System.Threading.CancellationToken]::None
do {
  $seg = [System.ArraySegment[byte]]::new($buffer)
  $r = $ws.ReceiveAsync($seg, $ct).GetAwaiter().GetResult()
  $mem.Write($buffer, 0, $r.Count)
} while (-not $r.EndOfMessage)
$ws.Dispose()

$resp = ([System.Text.Encoding]::UTF8.GetString($mem.ToArray())) | ConvertFrom-Json
if ($resp.error) { Write-Error "CDP error: $($resp.error.message)" }
[System.IO.File]::WriteAllBytes($PdfPath, [System.Convert]::FromBase64String($resp.result.data))
Write-Host "PDF saved: $PdfPath"