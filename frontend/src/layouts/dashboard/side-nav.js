import NextLink from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import PropTypes from 'prop-types';
import {
  Box,
  Divider,
  Drawer,
  Stack,
  SvgIcon,
  Typography,
  useMediaQuery
} from '@mui/material';
import { Logo } from '/src/components/logo';
import { Scrollbar } from '/src/components/scrollbar';
import CasinoIcon from '@mui/icons-material/Casino';
import { SideNavItem } from './side-nav-item';
import ChartBarIcon from '@heroicons/react/24/solid/ChartBarIcon';
import { ROLES } from '../../helpers/helper-constants';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { getUserRole } from '../../helpers/helper-functions';
import Button from '@mui/material/Button';
import { useDisconnect } from 'wagmi';
import { useAuth } from '../../hooks/use-auth';

const items = [
  {
    title: 'Overview',
    path: '/',
    icon: (
      <SvgIcon fontSize="small">
        <ChartBarIcon/>
      </SvgIcon>
    ),
    roles: [ROLES.ROLE_ADMIN, ROLES.ROLE_USER]
  },
  {
    title: 'Games',
    path: '/games',
    icon: (
      <SvgIcon fontSize="small">
        <CasinoIcon/>
      </SvgIcon>
    ),
    roles: [ROLES.ROLE_ADMIN, ROLES.ROLE_USER]
  },
  {
    title: 'Help',
    path: '/help',
    icon: (
      <SvgIcon fontSize="small">
        <HelpOutlineIcon/>
      </SvgIcon>
    ),
    roles: [ROLES.ROLE_ADMIN, ROLES.ROLE_USER]
  }
];
export const SideNav = (props) => {
  const { open, onClose } = props;
  const pathname = usePathname();
  const role = getUserRole();
  const auth = useAuth();
  const lgUp = useMediaQuery((theme) => theme.breakpoints.up('lg'));
  const currentItems = items.filter(item => item.roles.includes(role));
  const { disconnect } = useDisconnect();
  const router = useRouter();

  const signOut = async () => {
    disconnect();
    await auth.signOut();
    window.sessionStorage.clear();
    await router.push('/auth/login');
  };
  const content = (
    <Scrollbar
      sx={{
        height: '100%',
        '& .simplebar-content': {
          height: '100%'
        },
        '& .simplebar-scrollbar:before': {
          background: 'neutral.400'
        }
      }}
    >
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          height: '100%'
        }}
      >
        <Box sx={{ p: 3 }}>
          <Box
            component={NextLink}
            href="/"
            sx={{
              display: 'inline-flex',
              height: 32,
              width: 32
            }}
          >
            <Logo/>
          </Box>
          <Box
            sx={{
              alignItems: 'center',
              backgroundColor: 'rgba(255, 255, 255, 0.04)',
              borderRadius: 1,
              cursor: 'pointer',
              display: 'flex',
              justifyContent: 'space-between',
              mt: 2,
              p: '12px'
            }}
          >
            <div>
              <Typography
                color="inherit"
                variant="subtitle1"
              >
                eCrypt
              </Typography>
              <Typography
                color="neutral.400"
                variant="body2"
              >
                Dashboard
              </Typography>
            </div>
          </Box>
        </Box>
        <Divider sx={{ borderColor: 'neutral.700' }}/>
        <Box
          component="nav"
          sx={{
            flexGrow: 1,
            px: 2,
            py: 3
          }}
        >
          <Stack
            component="ul"
            spacing={0.5}
            sx={{
              listStyle: 'none',
              p: 0,
              m: 0
            }}
          >
            {currentItems.map((item) => {
              const active = item.path ? (pathname === item.path) : false;

              return (
                <SideNavItem
                  active={active}
                  disabled={item.disabled}
                  external={item.external}
                  icon={item.icon}
                  key={item.title}
                  path={item.path}
                  title={item.title}
                />
              );
            })}
            <Button variant="outlined" onClick={signOut}> Sign out</Button>
          </Stack>
        </Box>
        <Divider sx={{ borderColor: 'neutral.700' }}/>
        <Box
          sx={{
            px: 2,
            py: 3
          }}
        >
        </Box>
      </Box>
    </Scrollbar>
  );

  if (lgUp) {
    return (
      <Drawer
        anchor="left"
        open
        PaperProps={{
          sx: {
            backgroundColor: 'neutral.800',
            color: 'common.white',
            width: 280
          }
        }}
        variant="permanent"
      >
        {content}
      </Drawer>
    );
  }

  return (
    <Drawer
      anchor="left"
      onClose={onClose}
      open={open}
      PaperProps={{
        sx: {
          backgroundColor: 'neutral.800',
          color: 'common.white',
          width: 280
        }
      }}
      sx={{ zIndex: (theme) => theme.zIndex.appBar + 100 }}
      variant="temporary"
    >
      {content}
    </Drawer>
  );
};

SideNav.propTypes = {
  onClose: PropTypes.func,
  open: PropTypes.bool
};
