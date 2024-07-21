import Head from 'next/head';
import { CacheProvider } from '@emotion/react';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { CssBaseline } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import { AuthConsumer, AuthProvider } from '/src/contexts/auth-context';
import { useNProgress } from '/src/hooks/use-nprogress';
import { createTheme } from '/src/theme';
import { createEmotionCache } from '/src/utils/create-emotion-cache';
import 'simplebar-react/dist/simplebar.min.css';
import { Web3Modal } from '../components/crypto/wagmi';
import { GoogleOAuthProvider } from '@react-oauth/google';

const clientSideEmotionCache = createEmotionCache();

const SplashScreen = () => null;

const App = (props) => {
  const { Component, emotionCache = clientSideEmotionCache, pageProps } = props;
  useNProgress();
  const getLayout = Component.getLayout ?? ((page) => page);

  const theme = createTheme();

  return (
    <GoogleOAuthProvider clientId="763939633854-8ecbevetiet432gbnq4usd75j4j68il3.apps.googleusercontent.com">
      <Web3Modal>
        <CacheProvider value={emotionCache}>
          <Head>
            <title>
              eCrypt
            </title>
            <meta
              name="viewport"
              content="initial-scale=1, width=device-width"
            />
          </Head>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <CssBaseline/>
                <AuthConsumer>
                  {
                    (auth) => auth.isLoading
                      ? <SplashScreen/>
                      : getLayout(
                        <Component {...pageProps} />
                      )
                  }
                </AuthConsumer>
              </ThemeProvider>
            </AuthProvider>
          </LocalizationProvider>
        </CacheProvider>
      </Web3Modal>
    </GoogleOAuthProvider>
  );
};

export default App;
