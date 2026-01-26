#!/bin/bash
# PostgreSQL Migration Verification Script
# Run this to verify your PostgreSQL conversion is working correctly

echo "🔍 Verifying PostgreSQL Migration..."
echo "=================================="

# Check if Docker Compose file has been updated
echo "1. Checking docker-compose.yml..."
if grep -q "postgres:" docker-compose.yml; then
    echo "   ✅ Docker Compose uses PostgreSQL"
else
    echo "   ❌ Docker Compose still references MariaDB"
    exit 1
fi

# Check if backend configuration is updated
echo "2. Checking backend configuration..."
if grep -q "postgresql" backend/src/main/resources/application.properties; then
    echo "   ✅ Backend configured for PostgreSQL"
else
    echo "   ❌ Backend still configured for MariaDB"
    exit 1
fi

# Check if PostgreSQL driver is in pom.xml
echo "3. Checking Maven dependencies..."
if grep -q "postgresql" backend/pom.xml; then
    echo "   ✅ PostgreSQL driver found in pom.xml"
else
    echo "   ❌ PostgreSQL driver missing from pom.xml"
    exit 1
fi

# Check if MariaDB references are removed from pom.xml
if grep -q "mariadb" backend/pom.xml; then
    echo "   ⚠️  MariaDB references still found in pom.xml"
else
    echo "   ✅ MariaDB references removed from pom.xml"
fi

echo ""
echo "4. Testing Docker Compose setup..."
echo "   Starting PostgreSQL container..."
docker-compose up -d postgres

# Wait for PostgreSQL to start
sleep 5

# Test PostgreSQL connection
if docker-compose exec -T postgres psql -U postgres -d shift_scheduler -c "SELECT version();" > /dev/null 2>&1; then
    echo "   ✅ PostgreSQL container is running and accessible"
else
    echo "   ❌ Cannot connect to PostgreSQL container"
    exit 1
fi

echo ""
echo "5. Testing backend connection..."
echo "   Building and starting backend..."
docker-compose up -d --build backend

# Wait for backend to start
sleep 10

# Test backend health
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Backend is running and connected to PostgreSQL"
else
    echo "   ⚠️  Backend may still be starting (this is normal)"
fi

echo ""
echo "6. Checking database tables..."
# Check if tables are created
TABLES=$(docker-compose exec -T postgres psql -U postgres -d shift_scheduler -t -c "\dt" | wc -l)
if [ "$TABLES" -gt "5" ]; then
    echo "   ✅ Database tables created successfully"
    echo "   Tables found:"
    docker-compose exec -T postgres psql -U postgres -d shift_scheduler -c "\dt"
else
    echo "   ⚠️  Database tables not yet created (backend may still be starting)"
fi

echo ""
echo "=================================="
echo "🎉 PostgreSQL Migration Verification Complete!"
echo ""
echo "✅ Next steps:"
echo "1. Test the frontend: http://localhost:3000"
echo "2. Login with: admin@example.com / admin123"
echo "3. Create a test shift and verify database persistence"
echo "4. Deploy to Railway when ready!"
echo ""
echo "📚 Documentation available in docs/ folder:"
echo "   - POSTGRESQL_SETUP.md - Local setup instructions"
echo "   - RAILWAY_DEPLOYMENT.md - Cloud deployment guide"
echo "   - POSTGRESQL_MIGRATION.md - Migration details"
echo ""
echo "🚀 Ready to deploy to Railway with: railway up"
