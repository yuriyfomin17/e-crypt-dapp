import Head from 'next/head';
import {Box, Container, Unstable_Grid2 as Grid} from '@mui/material';
import {Layout as DashboardLayout} from '/src/layouts/dashboard/layout';
import {OverviewFinance} from '/src/sections/overview/overview-finance';
import {OverviewLatestTransactions} from '/src/sections/overview/overview-latest-transactions';
import {EthUsdPrice} from '../helpers/helper-rest-functions';
import {useEffect, useState} from 'react';
import {UserGameForm} from '../sections/overview/user-game-form';

import {convertToUsdt, getBalanceInfo, getUserRole} from '../helpers/helper-functions';
import {OverviewLatestGameTransactions} from '../sections/overview/overview-latest-game-transactions';
import {ROLES} from '../helpers/helper-constants';
import {OverviewLatestAdminGames} from '../sections/overview/overview-latest-admin-games';
import {TopUpForm} from '../sections/crypto-top-up-withdraw/top-up';

const PROFIT = 'PROFIT';
const BALANCE = 'BALANCE';

const Page = () => {
    const role = getUserRole()
    const {ethBalance, totalEthProfitEarned} = getBalanceInfo()
    const [balanceInfo, setBalanceInfo] = useState({
        ethBalance,
        totalEthProfitEarned
    });

    const {ethUsdPrice} = EthUsdPrice();
    const [isNewGameFormOpened, setIsNewGameFormOpened] = useState(false);
    const [isTopUpForm, setTopForm] = useState(false);
    useEffect(() => {
        const timer = setInterval(() => {
            setBalanceInfo(getBalanceInfo());
        }, 1000);
        return () => {
            clearInterval(timer);
        };
    }, []);
    return (
        <>
            <Head>
                <title>
                    Overview | eCrypt
                </title>
            </Head>

            <UserGameForm setIsNewGameFormOpened={setIsNewGameFormOpened}
                          isNewGameFormOpened={isNewGameFormOpened}
            />

            <TopUpForm
                isTopUpForm={isTopUpForm}
                setTopForm={setTopForm}
            />
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    py: 8
                }}
            >
                <Container maxWidth="xl">
                    <Grid
                        container
                        spacing={3}
                    >
                        <Grid
                            xs={12}
                            sm={6}
                            lg={6}
                        >
                            <OverviewFinance
                                setIsNewGameFormOpened={setIsNewGameFormOpened}
                                type={BALANCE}
                                ethUsdPrice={convertToUsdt(balanceInfo.ethBalance, ethUsdPrice)}
                                sx={{height: '100%'}}
                                ethBalance={balanceInfo.ethBalance}
                                setTopForm={setTopForm}
                            />
                        </Grid>
                        <Grid
                            xs={12}
                            sm={6}
                            lg={6}
                        >
                            <OverviewFinance
                                setIsNewGameFormOpened={setIsNewGameFormOpened}
                                type={PROFIT}
                                ethUsdPrice={convertToUsdt(balanceInfo.totalEthProfitEarned, ethUsdPrice)}
                                sx={{height: '100%'}}
                                ethBalance={balanceInfo.totalEthProfitEarned}
                            />
                        </Grid>
                        <Grid
                            xs={12}
                            md={12}
                            lg={12}
                        >
                            <OverviewLatestTransactions/>
                        </Grid>
                        {role === ROLES.ROLE_USER &&
                            <Grid
                                xs={12}
                                md={12}
                                lg={12}
                            >
                                <OverviewLatestGameTransactions/>
                            </Grid>
                        }

                        {role === ROLES.ROLE_ADMIN &&
                            <Grid
                                xs={12}
                                md={12}
                                lg={12}
                            >
                                <OverviewLatestAdminGames/>
                            </Grid>
                        }

                    </Grid>
                </Container>
            </Box>
        </>
    );
};

Page.getLayout = (page) => (
    <DashboardLayout>
        {page}
    </DashboardLayout>
);

export default Page;
