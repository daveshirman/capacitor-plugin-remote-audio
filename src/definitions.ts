export enum OrientationChoice {
  Unlocked = "unlocked",
  Portrait = "portrait",
  Landscape = "landscape"
}

export interface RemoteAudioPlugin {
  setOrientation(options: { orientation: OrientationChoice }): Promise<void>;
  getMediaInfo(options: { title: string, url: string }): Promise<{ info: any }>;
  play(options: { url: string }): Promise<{ isPlaying: boolean }>;
  pause(): Promise<void>;
  stop(): Promise<void>;
  seek(options: { milliseconds: number }): Promise<void>;
  getCurrentPosition(): Promise<{ position: number }>;
  checkOrRequestStoragePermissions(): Promise<{ hasStoragePermissions: boolean }>;
  downloadMedia(options: { id: string, url: string, folderName: string }): Promise<{ downloadInfo: any }>;
  cancelDownload(options: { id: string, folderName: string }): Promise<void>;
  deleteDownload(options: { id: string, folderName: string }): Promise<void>;
  deleteAllDownloads(options: { folderName: string }): Promise<void>;
  showRateApp(): Promise<void>;
}
