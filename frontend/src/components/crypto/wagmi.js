'use client';

import { createWeb3Modal, defaultWagmiConfig } from '@web3modal/wagmi/react';

import { WagmiConfig } from 'wagmi';
import { moonbaseAlpha } from 'viem/chains';

// 1. Get projectId
const projectId = '415bd5343c4fd3b731a6eaeb0cc4b78d';

// 2. Create wagmiConfig
const metadata = {
  name: 'Web3Modal',
  description: 'Web3Modal Example',
  url: 'https://web3modal.com',
  icons: ['https://avatars.githubusercontent.com/u/37784886']
};

const chains = [moonbaseAlpha];
const wagmiConfig = defaultWagmiConfig({ chains, projectId, metadata });

// 3. Create modal
export const WEB_MODAL = createWeb3Modal({ wagmiConfig, projectId, chains });

export const Web3Modal = ({ children }) => {
  return <WagmiConfig config={wagmiConfig}>{children}</WagmiConfig>;
};