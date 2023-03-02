import { WebPlugin } from '@capacitor/core';

import type { OrientationChoice, RemoteAudioPlugin } from './definitions';
import { Howl } from 'howler';

export class RemoteAudioWeb extends WebPlugin implements RemoteAudioPlugin {

  private playbackEnded: boolean = false;
  private howl: any;
  private maxMilliseconds: any = 0;

  constructor() {
    super();    
  }

  log(method: string, options?: any) {
    console.log(`RemoteAudioWeb: ${method}`);
    if (options) {
      console.log(`RemoteAudioWeb: ${JSON.stringify(options)}`);
    }
  }

  async setOrientation(options: { orientation: OrientationChoice }): Promise<void> {
    return new Promise(resolve => {
      this.log("setOrientation", options);
      resolve();
    });
  }

  async getMediaInfo(options: { title: string, url: string }): Promise<{ info: any }> {
    return new Promise(resolve => {
      // title is ignored on web wrapper
      this.log("getMediaInfo", options);
      try {
        this.playbackEnded = false;
        this.howl = new Howl({
          src: [options.url],
          html5: true,
          preload: true,
          onend: () => {
            this.playbackEnded = true;
          }
        });

        this.howl.once('load', () => {
          this.maxMilliseconds = this.howl.duration() * 1000;
          resolve({ 
            info: {
              duration: this.maxMilliseconds
            } 
          });
        });
      }
      catch(e) {
        this.log("getMediaInfo fail", e);
        this.howl = null;
        resolve({ 
          info: {
            duration: null
          } 
        });
      }
    })
  }

  async play(options: { url: string }): Promise<{ isPlaying: boolean }> {
    return new Promise(resolve => {
      try {
        this.log("play", options);
        this.howl.play();
        resolve({ isPlaying: true });
      }
      catch(e) {
        // whatever..
        resolve({ isPlaying: false })
      }
    });
  }
  
  async pause(): Promise<void> {
    this.log("pause");
    this.howl.pause();
    return;
  }

  async stop(): Promise<void> {
    this.log("stop");
    this.howl.stop();
    this.howl.unload();
    return;
  }

  async seek(options: { milliseconds: number }): Promise<void> {
    this.log("seek", options);
    this.howl.seek(options.milliseconds / 1000);
    return;
  }

  async getCurrentPosition(): Promise<{ position: number }> {
    return new Promise(resolve => {
      this.log("getCurrentPosition");
      try {
        if (this.playbackEnded) {
          resolve({ position: this.maxMilliseconds });
        }
        else {
          resolve({ position: this.howl.seek() * 1000 });
        }
      }
      catch(e) {
        this.log("getMediaInfo fail", e);
        this.howl.unload();
        resolve({ position: -1 });
      }
    });
  }

  
  async checkOrRequestStoragePermissions(): Promise<{ hasStoragePermissions: boolean }> {
    this.log("checkOrRequestStoragePermissions");
    // alert('plugin web.ts: fix');
    return Promise.resolve({ hasStoragePermissions: true });
  }

  async downloadMedia(options: { id: string, url: string, folderName: string }): Promise<{ downloadInfo: any }> {
    this.log("downloadMedia" + options);
    alert('plugin web.ts: fix');
    return new Promise(resolve => {
      resolve({ downloadInfo: null });
    });
  }

  async cancelDownload(options: { id: string, folderName: string }): Promise<void> {
    this.log("cancelDownload" + options);
    // alert('plugin web.ts: fix');
    return new Promise(resolve => {
      resolve();
    });
  }

  async deleteDownload(options: { id: string, folderName: string }): Promise<void> {
    this.log("deleteDownload" + options);
    // alert('plugin web.ts: fix');
    return new Promise(resolve => {
      resolve();
    });
  }

  async deleteAllDownloads(options: { folderName: string }): Promise<void> {
    this.log("deleteAllDownloads" + options);
    // alert('plugin web.ts: fix');
    return new Promise(resolve => {
      resolve();
    });
  }

  async showRateApp(): Promise<void> {
    this.log("showRateApp");
    // alert('plugin web.ts: fix');
    return Promise.resolve();
  }
}
