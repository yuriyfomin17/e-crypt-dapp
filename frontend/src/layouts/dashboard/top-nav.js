import PropTypes from 'prop-types';
import Bars3Icon from '@heroicons/react/24/solid/Bars3Icon';
import {
    Box,
    IconButton,
    Stack,
    SvgIcon,
    useMediaQuery
} from '@mui/material';
import {alpha} from '@mui/material/styles';
import Button from '@mui/material/Button';
import {useRouter} from 'next/navigation';
import {useAuth} from '../../hooks/use-auth';
import {useAccount, useDisconnect, useNetwork} from 'wagmi';
import {useCallback, useRef} from 'react';
import {useWeb3Modal} from '@web3modal/wagmi/react';
import {linkWalletAddress} from '../../helpers/helper-rest-functions';
import {ERROR_ICON, getAddress, toastMessage, updateAddress} from '../../helpers/helper-functions';
import {moonbaseAlpha} from "viem/chains";
import {BE_RESPONSES_MESSAGES} from "../../helpers/helper-constants";

const SIDE_NAV_WIDTH = 260;
const TOP_NAV_HEIGHT = 64;

export const TopNav = (props) => {
    const {onNavOpen} = props;
    const ref = useRef(true);
    const router = useRouter();
    const auth = useAuth();
    const {chain} = useNetwork()

    const {disconnect} = useDisconnect();
    const {open} = useWeb3Modal();
    const {isConnected} = useAccount({
        async onConnect({address}) {
            if (!chain.id || chain.id !== moonbaseAlpha.id){
                toastMessage(BE_RESPONSES_MESSAGES.INCORRECT_CHAIN_ID, ERROR_ICON)
                disconnect();
                return
            }
            const currentAddress = getAddress();
            if (!currentAddress) {
                updateAddress(address);
                const isLinked = await linkWalletAddress(address);
                if (!isLinked) {
                    disconnect();
                }
            }
        }
    });
    const handleSignOut = useCallback(
        async () => {
            if (ref.current) {
                ref.current = false;
                disconnect();
                await auth.signOut();
                window.sessionStorage.clear();
                await router.push('/auth/login');
            }

        },
        [disconnect]
    );
    const lgUp = useMediaQuery((theme) => theme.breakpoints.up('lg'));
    const connectWalletButton = (
        <Button
            x={{width: '100%', borderRadius: '20px', marginTop: '20px'}}
            variant="contained"
            onClick={() => open()}
        >
            Connect Wallet
        </Button>
    );

    const disconnectAndSignOutButton = (
        <Button sx={{width: '100%', borderRadius: '20px', marginTop: '20px'}}
                variant="contained"
                onClick={handleSignOut}
        >
            Disconnect and Sign Out
        </Button>
    );
    return (
        <>
            <Box
                component="header"
                sx={{
                    backdropFilter: 'blur(6px)',
                    backgroundColor: (theme) => alpha(theme.palette.background.default, 0.8),
                    position: 'sticky',
                    left: {
                        lg: `${SIDE_NAV_WIDTH}px`
                    },
                    top: 0,
                    width: {
                        lg: `calc(100% - ${SIDE_NAV_WIDTH}px)`
                    },
                    zIndex: 0
                }}
            >
                <Stack
                    alignItems="center"
                    direction="row"
                    justifyContent="space-between"
                    spacing={2}
                    sx={{
                        minHeight: TOP_NAV_HEIGHT,
                        px: 2
                    }}
                >
                    <Stack
                        alignItems="center"
                        direction="row"
                        spacing={2}
                    >
                        {!lgUp && (
                            <IconButton onClick={onNavOpen}>
                                <SvgIcon fontSize="small">
                                    <Bars3Icon/>
                                </SvgIcon>
                            </IconButton>
                        )}
                    </Stack>
                    <Stack
                        alignItems="center"
                        direction="row"
                        spacing={2}
                    >
                        {isConnected ? disconnectAndSignOutButton : connectWalletButton}
                    </Stack>
                </Stack>
            </Box>

        </>
    );
};

TopNav.propTypes = {
    onNavOpen: PropTypes.func
};
