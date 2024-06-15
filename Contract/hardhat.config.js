require("@nomicfoundation/hardhat-toolbox");
require("solidity-coverage");
require("dotenv").config();

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  defaultNetwork: "localhost",
  solidity: "0.8.24",
  settings: {
    optimizer: {
      enabled: true,
    },
  },
  etherscan: {
    apiKey: {
      bscTestnet: process.env.ETHERSCAN_API_KEY,
    },
  },
  networks: {
    hardhat: {
      blockGasLimit: 100000000429720, // whatever you want here
    },
    localhost: {
      url: process.env.API_URL,
    },
    testnet: {
      url: process.env.API_URL_TESTNET,
      chainId: 97,
      gasPrice: 20000000000,
      accounts: [process.env.PRIVATE_KEY_METAMASK],
    },
  },
  sourcify: {
    enabled: false,
    // Optional: specify a different Sourcify server
    apiUrl: "https://sourcify.dev/server",
    // Optional: specify a different Sourcify repository
    browserUrl: "https://repo.sourcify.dev",
  },
};