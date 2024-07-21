module.exports = {
  reactStrictMode: false,
  webpack: (config) => {
    config.externals.push(
      'pino-pretty',
      'lokijs',
      'encoding'
    );
    return config;
  }
};