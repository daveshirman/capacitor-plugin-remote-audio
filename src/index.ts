import { registerPlugin } from '@capacitor/core';

import type { RemoteAudioPlugin } from './definitions';

const RemoteAudio = registerPlugin<RemoteAudioPlugin>('RemoteAudio', {
  web: () => import('./web').then(m => new m.RemoteAudioWeb()),
});

export * from './definitions';
export { RemoteAudio };
