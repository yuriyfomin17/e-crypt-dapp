import { useCallback, useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';
import { styled } from '@mui/material/styles';
import { withAuthGuard } from '/src/hocs/with-auth-guard';
import { SideNav } from './side-nav';
import { TopNav } from './top-nav';
import { UpdateListener } from './update-listener';
import {
  getTokenExpiresAt,
  isTokenExpired,
  updateToken,
  updateTokenExpiresAt, updateTokenSalt
} from '../../helpers/helper-functions';
import { refreshToken } from '../../helpers/helper-rest-functions';

const SIDE_NAV_WIDTH = 280;

const LayoutRoot = styled('div')(({ theme }) => ({
  display: 'flex',
  flex: '1 1 auto',
  maxWidth: '100%',
  [theme.breakpoints.up('lg')]: {
    paddingLeft: SIDE_NAV_WIDTH
  }
}));

const LayoutContainer = styled('div')({
  display: 'flex',
  flex: '1 1 auto',
  flexDirection: 'column',
  width: '100%'
});

export const Layout = withAuthGuard((props) => {
  const { children } = props;
  const pathname = usePathname();
  const [openNav, setOpenNav] = useState(false);

  const handlePathnameChange = useCallback(
    () => {
      if (openNav) {
        setOpenNav(false);
      }
    },
    [openNav]
  );
  useEffect(() => {
    const timeInterval = setInterval(async () => {
      if (isTokenExpired(getTokenExpiresAt()) ) {
        const newUserData = await refreshToken();
        const {tokenSalt, token, tokenExpiresAt} = newUserData
        updateTokenSalt(tokenSalt)
        updateToken(token)
        updateTokenExpiresAt(tokenExpiresAt)
      }
    }, 15000);
    return () => clearInterval(timeInterval);
  }, [getTokenExpiresAt()]);

  useEffect(
    () => {
      handlePathnameChange();
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [pathname]
  );

  return (
    <>
      <TopNav onNavOpen={() => setOpenNav(true)}/>
      <SideNav
        onClose={() => setOpenNav(false)}
        open={openNav}
      />
        <UpdateListener>
          <LayoutRoot>
            <LayoutContainer>
              {children}
            </LayoutContainer>
          </LayoutRoot>
        </UpdateListener>
    </>
  );
});
