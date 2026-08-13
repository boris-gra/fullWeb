config.devServer = {
    ...(config.devServer || {}),
    port: 5010,
    open: {
        target: '/?b=http://localhost:5004',
    },
};
