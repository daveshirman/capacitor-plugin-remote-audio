# capacitor-plugin-remote-audio

Whatever

## Install

```bash
npm install capacitor-plugin-remote-audio
npx cap sync
```

## API

<docgen-index>

* [`setOrientation(...)`](#setorientation)
* [`getMediaInfo(...)`](#getmediainfo)
* [`play(...)`](#play)
* [`pause()`](#pause)
* [`stop()`](#stop)
* [`seek(...)`](#seek)
* [`getCurrentPosition()`](#getcurrentposition)
* [`checkOrRequestStoragePermissions()`](#checkorrequeststoragepermissions)
* [`downloadMedia(...)`](#downloadmedia)
* [`cancelDownload(...)`](#canceldownload)
* [`deleteDownload(...)`](#deletedownload)
* [`deleteAllDownloads(...)`](#deletealldownloads)
* [`showRateApp()`](#showrateapp)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### setOrientation(...)

```typescript
setOrientation(options: { orientation: OrientationChoice; }) => any
```

| Param         | Type                                                                              |
| ------------- | --------------------------------------------------------------------------------- |
| **`options`** | <code>{ orientation: <a href="#orientationchoice">OrientationChoice</a>; }</code> |

**Returns:** <code>any</code>

--------------------


### getMediaInfo(...)

```typescript
getMediaInfo(options: { title: string; url: string; }) => any
```

| Param         | Type                                         |
| ------------- | -------------------------------------------- |
| **`options`** | <code>{ title: string; url: string; }</code> |

**Returns:** <code>any</code>

--------------------


### play(...)

```typescript
play(options: { url: string; }) => any
```

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ url: string; }</code> |

**Returns:** <code>any</code>

--------------------


### pause()

```typescript
pause() => any
```

**Returns:** <code>any</code>

--------------------


### stop()

```typescript
stop() => any
```

**Returns:** <code>any</code>

--------------------


### seek(...)

```typescript
seek(options: { milliseconds: number; }) => any
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ milliseconds: number; }</code> |

**Returns:** <code>any</code>

--------------------


### getCurrentPosition()

```typescript
getCurrentPosition() => any
```

**Returns:** <code>any</code>

--------------------


### checkOrRequestStoragePermissions()

```typescript
checkOrRequestStoragePermissions() => any
```

**Returns:** <code>any</code>

--------------------


### downloadMedia(...)

```typescript
downloadMedia(options: { id: string; url: string; folderName: string; }) => any
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code>{ id: string; url: string; folderName: string; }</code> |

**Returns:** <code>any</code>

--------------------


### cancelDownload(...)

```typescript
cancelDownload(options: { id: string; folderName: string; }) => any
```

| Param         | Type                                             |
| ------------- | ------------------------------------------------ |
| **`options`** | <code>{ id: string; folderName: string; }</code> |

**Returns:** <code>any</code>

--------------------


### deleteDownload(...)

```typescript
deleteDownload(options: { id: string; folderName: string; }) => any
```

| Param         | Type                                             |
| ------------- | ------------------------------------------------ |
| **`options`** | <code>{ id: string; folderName: string; }</code> |

**Returns:** <code>any</code>

--------------------


### deleteAllDownloads(...)

```typescript
deleteAllDownloads(options: { folderName: string; }) => any
```

| Param         | Type                                 |
| ------------- | ------------------------------------ |
| **`options`** | <code>{ folderName: string; }</code> |

**Returns:** <code>any</code>

--------------------


### showRateApp()

```typescript
showRateApp() => any
```

**Returns:** <code>any</code>

--------------------


### Enums


#### OrientationChoice

| Members         | Value                    |
| --------------- | ------------------------ |
| **`Unlocked`**  | <code>"unlocked"</code>  |
| **`Portrait`**  | <code>"portrait"</code>  |
| **`Landscape`** | <code>"landscape"</code> |

</docgen-api>
