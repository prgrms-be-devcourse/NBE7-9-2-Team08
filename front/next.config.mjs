/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    serverActions: {
      bodySizeLimit: '2mb',
    },
  },
  async rewrites() {
    if (process.env.NEXT_PUBLIC_DEV_PROXY === 'true') {
      return [
        {
          source: "/api/:path((?!analysis/stream).*)",
          destination: `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/:path*`,
        },
      ];
    }
    return [];
  },
};

export default nextConfig;
